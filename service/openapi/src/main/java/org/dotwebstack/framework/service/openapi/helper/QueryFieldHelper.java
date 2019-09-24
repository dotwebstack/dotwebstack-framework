package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.Operation;
import java.util.HashMap;
import lombok.Builder;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;

@Builder
public class QueryFieldHelper {

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private GraphQlFieldBuilder graphQlFieldBuilder;

  public GraphQlField resolveGraphQlField(@NonNull Operation operation) {
    String dwsQuery = (String) operation.getExtensions()
        .get(X_DWS_QUERY);
    FieldDefinition queryFieldDefinition = getQueryFieldDefinition(dwsQuery);

    return this.graphQlFieldBuilder.toGraphQlField(queryFieldDefinition, new HashMap<>());
  }

  private FieldDefinition getQueryFieldDefinition(String dwsQuery) {
    ObjectTypeDefinition query = (ObjectTypeDefinition) this.typeDefinitionRegistry.getType("Query")
        .orElseThrow(() -> invalidConfigurationException("Type 'Query' not found in GraphQL schema."));
    return query.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals(dwsQuery))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException(
            "x-dws-query with value '{}' not found in GraphQL schema.", dwsQuery));
  }

}
