package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryName;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.Operation;
import java.util.HashMap;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;

@Builder
public class QueryFieldHelper {

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  private final GraphQlFieldBuilder graphQlFieldBuilder;

  public Optional<GraphQlField> resolveGraphQlField(@NonNull Operation operation) {
    return getDwsQueryName(operation).map(this::getQueryFieldDefinition)
        .map(queryFieldDefinition -> this.graphQlFieldBuilder.toGraphQlField(queryFieldDefinition, new HashMap<>()));
  }

  private FieldDefinition getQueryFieldDefinition(String dwsQuery) {
    ObjectTypeDefinition query = (ObjectTypeDefinition) this.typeDefinitionRegistry.getType("Query")
        .orElseThrow(() -> invalidConfigurationException("Type 'Query' not found in GraphQL schema."));
    return query.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals(dwsQuery))
        .findFirst()
        .orElseThrow(
            () -> invalidConfigurationException("x-dws-query with value '{}' not found in GraphQL schema.", dwsQuery));
  }

}
