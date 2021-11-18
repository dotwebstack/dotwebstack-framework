package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.datafetchers.ContextConstants.CONTEXT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FILTER_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.NODES_FIELD_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getObjectField;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isEnvelope;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isPageableField;

import graphql.ExecutionInput;
import graphql.language.Argument;
import graphql.language.AstPrinter;
import graphql.language.Field;
import graphql.language.IntValue;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.dotwebstack.framework.service.openapi.mapping.TypeMapper;
import org.dotwebstack.framework.service.openapi.query.paging.QueryPaging;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class QueryMapper {

  private static final String OPERATION_NAME = "Query";

  private static final Set<String> RESERVED_ARGS =
      Set.of(FILTER_ARGUMENT_NAME, SORT_ARGUMENT_NAME, CONTEXT_ARGUMENT_NAME);

  private final GraphQLSchema graphQlSchema;

  private final QueryArgumentBuilder queryArgumentBuilder;

  private final Map<String, TypeMapper> typeMappers;

  public QueryMapper(@NonNull GraphQLSchema graphQlSchema, @NonNull QueryArgumentBuilder queryArgumentBuilder,
      @NonNull Collection<TypeMapper> typeMappers) {
    this.graphQlSchema = graphQlSchema;
    this.queryArgumentBuilder = queryArgumentBuilder;
    this.typeMappers = typeMappers.stream()
        .collect(Collectors.toMap(TypeMapper::typeName, Function.identity()));
  }

  public ExecutionInput map(OperationRequest operationRequest) {
    var fieldName = operationRequest.getContext()
        .getQueryProperties()
        .getField();

    var queryType = graphQlSchema.getQueryType();
    var fieldDefinition = getObjectField(queryType, fieldName);
    var fieldArguments = mapArguments(fieldDefinition, graphQlSchema.getQueryType(), operationRequest);
    var queryField = new Field(fieldName, fieldArguments, new SelectionSet(
        mapSchema(operationRequest.getResponseSchema(), fieldDefinition).collect(Collectors.toList())));

    var query = OperationDefinition.newOperationDefinition()
        .name(OPERATION_NAME)
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(new SelectionSet(List.of(queryField)))
        .build();

    return ExecutionInput.newExecutionInput()
        .query(AstPrinter.printAst(query))
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();
  }

  private Stream<Field> mapSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition) {
    if (schema instanceof ComposedSchema) {
      throw invalidConfigurationException("Unsupported composition construct oneOf / anyOf encountered.");
    }

    if (schema instanceof ArraySchema) {
      return mapArraySchema((ArraySchema) schema, fieldDefinition);
    }

    if ("object".equals(schema.getType())) {
      return mapObjectSchema(schema, fieldDefinition);
    }

    return Stream.empty();
  }

  private Stream<Field> mapArraySchema(ArraySchema schema, GraphQLFieldDefinition fieldDefinition) {
    if (isPageableField(fieldDefinition)) {
      var nestedFieldDefinition =
          ((GraphQLObjectType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType())).getField(NODES_FIELD_NAME);

      return Stream.of(new Field(NODES_FIELD_NAME,
          new SelectionSet(mapSchema(schema.getItems(), nestedFieldDefinition).collect(Collectors.toList()))));
    }

    var itemsSchema = schema.getItems();

    if ("object".equals(itemsSchema.getType())) {
      return mapSchema(schema.getItems(), fieldDefinition);
    }

    if (itemsSchema instanceof ComposedSchema) {
      throw invalidConfigurationException("Unsupported composition construct oneOf / anyOf encountered.");
    }

    return Stream.of();
  }

  private Stream<Field> mapObjectSchema(Schema<?> schema, GraphQLFieldDefinition fieldDefinition) {
    return schema.getProperties()
        .entrySet()
        .stream()
        .flatMap(entry -> mapObjectSchemaProperty(entry.getKey(), entry.getValue(), fieldDefinition));
  }

  private Stream<Field> mapObjectSchemaProperty(String name, Schema<?> schema,
      GraphQLFieldDefinition parentFieldDefinition) {
    var rawParentType = GraphQLTypeUtil.unwrapAll(parentFieldDefinition.getType());

    if (!(rawParentType instanceof GraphQLObjectType)) {
      throw invalidConfigurationException("Object schema does not match GraphQL field type (found: {}).",
          rawParentType.getName());
    }

    var objectType = (GraphQLObjectType) rawParentType;
    var fieldDefinition = objectType.getFieldDefinition(name);

    if (fieldDefinition == null || isEnvelope(schema)) {
      return mapSchema(schema, parentFieldDefinition);
    }

    var rawType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (typeMappers.containsKey(rawType.getName())) {
      return typeMappers.get(rawType.getName())
          .schemaToField(name, schema)
          .stream();
    }

    if (!isSchemaNullabilityValid(schema, fieldDefinition, (GraphQLObjectType) rawParentType)) {
      throw invalidConfigurationException(
          "Nullability of `{}` of type {} in response schema is stricter than GraphQL schema.", name, schema.getClass()
              .getSimpleName());
    }

    if (schema instanceof ArraySchema) {
      var fields = mapArraySchema((ArraySchema) schema, fieldDefinition).collect(Collectors.toList());

      if (fields.isEmpty()) {
        return Stream.of(new Field(fieldDefinition.getName()));
      }

      return Stream.of(new Field(fieldDefinition.getName(), new SelectionSet(fields)));
    }

    var nestedFields = mapSchema(schema, fieldDefinition).collect(Collectors.toList());

    if (nestedFields.isEmpty()) {
      return Stream.of(new Field(name));
    }

    return Stream.of(new Field(name, new SelectionSet(nestedFields)));
  }

  private List<Argument> mapArguments(GraphQLFieldDefinition fieldDefinition,
      GraphQLFieldsContainer parentFieldsContainer, OperationRequest operationRequest) {
    var parameters = new HashMap<>(operationRequest.getParameters());
    if (MapperUtils.isPageableField(fieldDefinition)) {
      parameters.putAll(QueryPaging.toPagingArguments(operationRequest.getContext()
          .getQueryProperties()
          .getPaging(), operationRequest.getParameters()));
    }

    List<Argument> result = fieldDefinition.getArguments()
        .stream()
        .filter(argument -> !RESERVED_ARGS.contains(argument.getName()))
        .flatMap(argument -> Stream.ofNullable(parameters.get(argument.getName()))
            .map(parameterValue -> new Argument(argument.getName(), mapArgument(argument, parameterValue))))
        .collect(Collectors.toList());

    if (fieldDefinition.getArgument(FILTER_ARGUMENT_NAME) != null
        && MapperUtils.isQueryField(fieldDefinition, parentFieldsContainer)) {
      result.addAll(queryArgumentBuilder.buildArguments(operationRequest));
    }
    return result;
  }

  private Value<?> mapArgument(GraphQLArgument argument, Object parameterValue) {
    GraphQLInputType inputType = argument.getType();
    if (inputType instanceof GraphQLScalarType) {
      String type = ((GraphQLScalarType) inputType).getName();
      switch (type) {
        case "Int":
          if (parameterValue instanceof Integer) {
            return IntValue.of((Integer) parameterValue);
          }

          throw invalidConfigurationException("Could not map parameter value {} for GraphQL argument {} of type Int",
              parameterValue, argument.getName());
        case "String":
          return StringValue.of(String.valueOf(parameterValue));
        default:
          throw invalidConfigurationException("Could not map parameter value {} for GraphQL argument {} of type {}",
              parameterValue, argument.getName(), type);
      }
    }

    return StringValue.of(String.valueOf(parameterValue));
  }

  private boolean isSchemaNullabilityValid(Schema<?> schema, GraphQLFieldDefinition fieldDefinition,
      GraphQLFieldsContainer parentFieldsContainer) {
    var schemaNullable = schema.getNullable() != null && schema.getNullable();

    if (schemaNullable) {
      return true;
    }

    var fieldType = fieldDefinition.getType();

    if (schema instanceof ObjectSchema || schema instanceof ArraySchema) {
      // unwrap GraphQL Type in non-nullable List, e.g. [Type]!
      if (fieldType instanceof GraphQLNonNull && ((GraphQLNonNull) fieldType).getWrappedType() instanceof GraphQLList) {
        var listItemType = ((GraphQLList) ((GraphQLNonNull) fieldType).getWrappedType()).getWrappedType();
        return listItemType instanceof GraphQLNonNull;
      }

      // unwrap GraphQL Type in nullable List, e.g. [Type]
      if (fieldType instanceof GraphQLList) {
        var listItemType = ((GraphQLList) fieldType).getWrappedType();
        return listItemType instanceof GraphQLNonNull;
      }

      // top-level query Object result type
      if (MapperUtils.isQueryField(fieldDefinition, parentFieldsContainer)) {
        return true;
      }
    }

    return fieldType instanceof GraphQLNonNull;
  }
}
