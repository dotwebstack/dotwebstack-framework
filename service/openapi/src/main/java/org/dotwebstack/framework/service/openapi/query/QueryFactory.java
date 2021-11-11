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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.helper.SchemaResolver;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class QueryFactory {

  private final OpenAPI openApi;

  private final GraphQLSchema graphQlSchema;

  public QueryFactory(OpenAPI openApi, GraphQLSchema graphQlSchema) {
    this.openApi = openApi;
    this.graphQlSchema = graphQlSchema;
  }

  public ExecutionInput create(OperationRequest operationRequest) {
    var operationContext = operationRequest.getContext();
    var dwsQuerySettings = DwsExtensionHelper.getDwsQuerySettings(operationContext.getOperation());

    if (dwsQuerySettings.getSelectionSet() != null) {
      throw new UnsupportedOperationException();
    }

    var responseSchema = operationContext.getSuccessResponse()
        .getContent()
        .get(operationRequest.getPreferredMediaType())
        .getSchema();

    var queryField = createField(dwsQuerySettings.getQueryName(), responseSchema, graphQlSchema.getQueryType());

    var query = OperationDefinition.newOperationDefinition()
        .name("Query")
        .operation(OperationDefinition.Operation.QUERY)
        .selectionSet(new SelectionSet(List.of(queryField)))
        .build();

    return ExecutionInput.newExecutionInput()
        .query(AstPrinter.printAst(query))
        .build();
  }

  private Stream<Field> createFields(Schema<?> schema, GraphQLFieldDefinition fieldDefinition) {
    if (schema.get$ref() != null) {
      return createFields(SchemaResolver.resolveSchema(openApi, schema.get$ref()), fieldDefinition);
    }

    if (schema instanceof ArraySchema) {
      return createFields(((ArraySchema) schema).getItems(), fieldDefinition);
    }

    if (!(schema instanceof ObjectSchema)) {
      return Stream.empty();
    }

    if (isEnvelope(schema)) {
      return schema.getProperties()
          .entrySet()
          .stream()
          .flatMap(entry -> createFields(entry.getValue(), fieldDefinition));
    }

    var fieldType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (!(fieldType instanceof GraphQLObjectType)) {
      throw invalidConfigurationException("Type is not an object.");
    }

    return schema.getProperties()
        .entrySet()
        .stream()
        .map(entry -> createField(entry.getKey(), entry.getValue(), (GraphQLObjectType) fieldType));
  }

  private boolean isEnvelope(Schema<?> schema) {
    return Optional.ofNullable(schema.getExtensions())
        .map(extensions -> Boolean.TRUE.equals(extensions.get(OasConstants.X_DWS_ENVELOPE)))
        .orElse(false);
  }

  private Field createField(String name, Schema<?> schema, GraphQLObjectType objectType) {
    var fieldDefinition = Optional.ofNullable(objectType.getFieldDefinition(name))
        .orElseThrow(
            () -> invalidConfigurationException("Field '{}' not found for `{}` type.", name, objectType.getName()));

    var rawFieldType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (rawFieldType instanceof GraphQLObjectType) {
      return new Field(name, new SelectionSet(createFields(schema, fieldDefinition).collect(Collectors.toList())));
    }

    return new Field(name);
  }
}
