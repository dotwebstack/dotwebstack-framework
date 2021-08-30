package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.query.paging.PagingHelper.addPaging;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.Feature;
import org.dotwebstack.framework.service.openapi.query.filter.FilterHelper;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.dwssettings.DwsQuerySettings;
import org.springframework.stereotype.Component;

@Component
public class GraphQlQueryBuilder {

  private final boolean pagingEnabled;

  private final JexlEngine jexlEngine;

  public GraphQlQueryBuilder(@NonNull DotWebStackConfiguration dwsConfig, @NonNull JexlEngine jexlEngine) {
    this.pagingEnabled = dwsConfig.isFeatureEnabled(Feature.PAGING);
    this.jexlEngine = jexlEngine;
  }

  public Optional<QueryInput> toQueryInput(@NonNull ResponseSchemaContext responseSchemaContext,
      @NonNull Map<String, Object> inputParams) {

    DwsQuerySettings dwsQuerySettings = responseSchemaContext.getDwsQuerySettings();
    String queryName = dwsQuerySettings.getQueryName();

    if (queryName == null || queryName.isEmpty()) {
      return Optional.empty();
    }

    ResponseTemplate okResponse = getOkResponse(responseSchemaContext);

    Optional<Field> rootField = OasToGraphQlHelper.toQueryField(queryName, okResponse, inputParams, this.pagingEnabled);
    if (rootField.isEmpty()) {
      return Optional.empty();
    }
    GraphQlQuery query = toQueryInput(rootField.get());

    FilterHelper filterHelper = new FilterHelper(this.jexlEngine, inputParams);
    filterHelper.addKeys(query, dwsQuerySettings.getKeys());
    Map<String, Object> variables;
    variables = filterHelper.addFilters(query, responseSchemaContext.getDwsQuerySettings()
        .getFilters());
    filterHelper.addFilters(query, dwsQuerySettings.getFilters());
    addPaging(query, dwsQuerySettings.getPaging(), inputParams);

    return Optional.of(QueryInput.builder()
        .query(query.toString())
        .variables(variables)
        .build());
  }

  private GraphQlQuery toQueryInput(Field rootField) {
    GraphQlQuery.GraphQlQueryBuilder builder = GraphQlQuery.builder();
    builder.field(rootField);
    builder.queryName("Query");
    return builder.build();

  }

  private ResponseTemplate getOkResponse(ResponseSchemaContext responseSchemaContext) {
    return responseSchemaContext.getResponses()
        .stream()
        .filter(r -> r.getResponseCode() == 200)
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("No OK response found"));
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
