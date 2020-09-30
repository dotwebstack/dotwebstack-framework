package org.dotwebstack.framework.backend.json.directives;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.backend.json.query.JsonQueryFetcher;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class JsonDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private final JsonQueryFetcher jsonQueryFetcher;

  public JsonDirectiveWiring(@NonNull JsonQueryFetcher jsonQueryFetcher) {
    this.jsonQueryFetcher = jsonQueryFetcher;
  }

  @Override
  public String getDirectiveName() {
    return JsonDirectives.JSON_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLType outputType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    //todo jsondirective validation

    registerDataFetcher(environment);

    return fieldDefinition;
  }

  private void registerDataFetcher(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLCodeRegistry.Builder codeRegistry = environment.getCodeRegistry();

    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    GraphQLFieldDefinition fieldDefinition = environment.getElement();

    codeRegistry.dataFetcher(fieldsContainer, fieldDefinition, jsonQueryFetcher);
  }
}
