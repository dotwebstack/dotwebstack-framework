package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.ExecutionInput;
import graphql.language.AstPrinter;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.SchemaResolver;
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

    var queryField =
        mapField(queryProperties.getField(), operationRequest.getResponseSchema(), graphQlSchema.getQueryType());

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

  private Stream<Field> mapFields(Schema<?> schema, GraphQLFieldDefinition fieldDefinition) {
    if (schema.get$ref() != null) {
      return mapFields(SchemaResolver.resolveSchema(openApi, schema.get$ref()), fieldDefinition);
    }

    if (schema instanceof ArraySchema) {
      return mapFields(((ArraySchema) schema).getItems(), fieldDefinition);
    }

    if (!(schema instanceof ObjectSchema)) {
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
        .map(entry -> mapField(entry.getKey(), entry.getValue(), (GraphQLObjectType) fieldType));
  }

  private Field mapField(String name, Schema<?> schema, GraphQLObjectType objectType) {
    var fieldDefinition = MapperUtils.getObjectField(objectType, name);
    var rawFieldType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (rawFieldType instanceof GraphQLObjectType) {
      return new Field(name, new SelectionSet(mapFields(schema, fieldDefinition).collect(Collectors.toList())));
    }

    return new Field(name);
  }
}
