package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getObjectField;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isEnvelope;

import graphql.ExecutionInput;
import graphql.language.Argument;
import graphql.language.AstPrinter;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class QueryMapper {

  private static final String OPERATION_NAME = "Query";

  private static final Set<String> RESERVED_ARGS = Set.of("filter", "sort", "context");

  private final GraphQLSchema graphQlSchema;

  public QueryMapper(GraphQLSchema graphQlSchema) {
    this.graphQlSchema = graphQlSchema;
  }

  public ExecutionInput map(OperationRequest operationRequest) {
    var fieldName = operationRequest.getContext()
        .getQueryProperties()
        .getField();

    var fieldDefinition = getObjectField(graphQlSchema.getQueryType(), fieldName);

    var queryField =
        mapField(fieldName, operationRequest.getResponseSchema(), fieldDefinition, operationRequest.getParameters());

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

  private Field mapField(String name, Schema<?> schema, GraphQLFieldDefinition fieldDefinition,
      Map<String, Object> parameters) {
    if (schema instanceof ComposedSchema) {
      throw invalidConfigurationException("Unsupported composition construct oneOf / anyOf encountered.");
    }

    var arguments = mapArguments(fieldDefinition, parameters);

    if (schema instanceof ArraySchema) {
      return mapField(name, ((ArraySchema) schema).getItems(), fieldDefinition, parameters);
    }

    // Composed schemas are type-less, but allOf should only be used on objects
    if (!(schema instanceof ObjectSchema) && schema.getType() != null) {
      return new Field(name);
    }

    var selections = mapObjectFields(schema, fieldDefinition, parameters).collect(Collectors.toList());

    return new Field(name, arguments, new SelectionSet(selections));
  }

  private Stream<Field> mapObjectFields(Schema<?> schema, GraphQLFieldDefinition fieldDefinition,
      Map<String, Object> parameters) {
    if (isEnvelope(schema)) {
      return schema.getProperties()
          .values()
          .stream()
          .flatMap(nestedSchema -> mapObjectFields(nestedSchema, fieldDefinition, parameters));
    }

    if (schema instanceof ArraySchema) {
      return mapObjectFields(((ArraySchema) schema).getItems(), fieldDefinition, parameters);
    }

    var rawType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (!(rawType instanceof GraphQLObjectType)) {
      throw invalidConfigurationException("Object schema does not match GraphQL field type (found: {}).",
          rawType.getName());
    }

    return schema.getProperties()
        .entrySet()
        .stream()
        .map(entry -> mapField(entry.getKey(), entry.getValue(),
            getObjectField((GraphQLObjectType) rawType, entry.getKey()), parameters));
  }

  private List<Argument> mapArguments(GraphQLFieldDefinition fieldDefinition, Map<String, Object> parameters) {
    return fieldDefinition.getArguments()
        .stream()
        .filter(argument -> !RESERVED_ARGS.contains(argument.getName()))
        .flatMap(argument -> Stream.ofNullable(parameters.get(argument.getName()))
            .map(parameterValue -> new Argument(argument.getName(), mapArgument(argument, parameterValue))))
        .collect(Collectors.toList());
  }

  private Value<?> mapArgument(GraphQLArgument argument, Object parameterValue) {
    // TODO: support other types
    return StringValue.of(String.valueOf(parameterValue));
  }
}
