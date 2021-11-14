package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.ExecutionInput;
import graphql.language.Argument;
import graphql.language.AstPrinter;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class QueryMapper {

  private final OpenAPI openApi;

  private final GraphQLSchema graphQlSchema;

  public QueryMapper(OpenAPI openApi, GraphQLSchema graphQlSchema) {
    this.openApi = openApi;
    this.graphQlSchema = graphQlSchema;
  }

  public ExecutionInput map(OperationRequest operationRequest) {
    var queryProperties = operationRequest.getContext()
        .getQueryProperties();

    var queryField = mapField(queryProperties.getField(), operationRequest.getResponseSchema(),
        graphQlSchema.getQueryType(), createKeyArguments(operationRequest));

    var query = OperationDefinition.newOperationDefinition()
        .name("Query")
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(new SelectionSet(List.of(queryField)))
        .build();

    return ExecutionInput.newExecutionInput()
        .query(AstPrinter.printAst(query))
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();
  }

  private Field mapField(String name, Schema<?> schema, GraphQLFieldsContainer fieldsContainer,
      List<Argument> arguments) {
    var fieldDefinition = MapperUtils.getObjectField(fieldsContainer, name);
    var selectionSet = mapFields(schema, fieldDefinition).collect(Collectors.toList());

    if (selectionSet.isEmpty()) {
      return new Field(name);
    }

    return new Field(name, arguments,
        new SelectionSet(mapFields(schema, fieldDefinition).collect(Collectors.toList())));
  }

  private Stream<Field> mapFields(Schema<?> schema, GraphQLFieldDefinition fieldDefinition) {
    if (schema instanceof ArraySchema) {
      return mapFields(((ArraySchema) schema).getItems(), fieldDefinition);
    }

    if (!(schema instanceof ObjectSchema)) {
      if (schema instanceof ComposedSchema) {
        validateComposedSchema((ComposedSchema) schema);
      }

      return Stream.empty();
    }

    if (MapperUtils.isEnvelope(schema)) {
      return schema.getProperties()
          .entrySet()
          .stream()
          .flatMap(entry -> mapFields(entry.getValue(), fieldDefinition));
    }

    var fieldType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (!(fieldType instanceof GraphQLObjectType)) {
      throw invalidConfigurationException("Type is not an object.");
    }

    return schema.getProperties()
        .entrySet()
        .stream()
        .map(entry -> mapField(entry.getKey(), entry.getValue(), (GraphQLObjectType) fieldType, List.of()));
  }

  private List<Argument> createKeyArguments(OperationRequest operationRequest) {
    Map<String, Object> paramKeyValues = operationRequest.getContext()
        .getQueryProperties()
        .getKeys()
        .entrySet()
        .stream()
        .filter(e -> operationRequest.getParameters()
            .containsKey(paramKeyFromPath(e.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> operationRequest.getParameters()
            .get(paramKeyFromPath(e.getValue()))));

    return paramKeyValues.entrySet()
        .stream()
        .map(e -> new Argument(e.getKey(), toArgumentValue(e.getValue())))
        .collect(Collectors.toList());
  }

  private Value toArgumentValue(Object e) {
    if (e instanceof String) {
      return new StringValue((String) e);
    } else {
      // TODO: support other types
      return new StringValue(e.toString());
    }
  }

  private String paramKeyFromPath(String path) {
    return path.substring(path.lastIndexOf(".") + 1);
  }

  private void validateComposedSchema(ComposedSchema composedSchema) {
    String construct;
    if (composedSchema.getOneOf() != null) {
      construct = "oneOf";
    } else if (composedSchema.getAnyOf() != null) {
      construct = "anyOf";
    } else if (composedSchema.getAllOf() != null) {
      throw illegalStateException(
          "Encountered allOf construct. This is unexpected because the OpenAPI spec should already be fully resolved.");
    } else {
      throw illegalStateException("Unknown composition construct {} encountered for schema:%n{}", composedSchema);
    }

    throw invalidConfigurationException("Unsupported composition construct {} used.", construct);
  }
}
