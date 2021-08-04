package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.query.filter.FilterHelper.addFilters;
import static org.dotwebstack.framework.service.openapi.query.filter.FilterHelper.addSelections;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.DwsQuerySettings;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.springframework.http.MediaType;

public class GraphQlQueryBuilder {

  public Optional<String> toQuery(@NonNull ResponseSchemaContext responseSchemaContext,
      @NonNull Map<String, Object> inputParams, MediaType mediaType) {

    DwsQuerySettings dwsQuerySettings = responseSchemaContext.getDwsQuerySettings();
    String queryName = dwsQuerySettings.getQueryName();

    if (queryName == null || queryName.isEmpty()) {
      return Optional.empty();
    }

    ResponseTemplate okResponse = responseSchemaContext.getResponses()
        .stream()
        .filter(r -> r.getResponseCode() == 200)
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException("No OK response found"));

    List<Field> fields = OasToGraphQlHelper.toQueryFields(okResponse, inputParams);
    Optional<GraphQlQuery> query = toQuery(queryName, fields);
    query.ifPresent(q -> addSelections(q, responseSchemaContext.getRequestBodyContext(),
        responseSchemaContext.getParameters(), inputParams, mediaType));
    query.ifPresent(q -> addFilters(q, responseSchemaContext.getDwsQuerySettings()
        .getFilters(), inputParams));

    return query.map(GraphQlQuery::toString);
  }

  private Optional<GraphQlQuery> toQuery(String queryName, List<Field> fields) {
    Field root = new Field();
    root.setChildren(fields);
    root.setName(queryName);

    GraphQlQuery.GraphQlQueryBuilder builder = GraphQlQuery.builder();
    builder.field(root);
    builder.queryName("Wrapper");
    return Optional.of(builder.build());

  }

  protected void validateRequiredPathsQueried(Set<String> requiredPaths, Set<String> queriedPaths) {
    /*
     * This method checks if the paths that are required from the OAS schema are added to the GraphQL
     * query. This is checked so that we fail fast, a request now always fails if a field is missing and
     * we can build our tests on that assumption
     */
    List<String> missingPaths = requiredPaths.stream()
        .filter(requiredPath -> !queriedPaths.contains(requiredPath))
        .collect(Collectors.toList());

    if (!missingPaths.isEmpty()) {
      throw invalidConfigurationException(
          "the following paths are required from OAS, but could not be mapped on the GraphQL schema: '{}'",
          String.join(", ", missingPaths));
    }
  }

}
