package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.response.DwsQuerySettings;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;

public class GraphQlQueryBuilder {

  public Optional<String> toQuery(@NonNull ResponseSchemaContext responseSchemaContext,
      @NonNull Map<String, Object> inputParams) {

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

    List<Field> fields = OasToGraphQlHelper.toFields(okResponse, inputParams);
    return toQuery(queryName, fields);
  }

  private Optional<String> toQuery(String queryName, List<Field> nodes) {
    Field root = new Field();
    root.setChildren(nodes);
    root.setName(queryName);

    ResourceQuery.ResourceQueryBuilder builder = ResourceQuery.builder();
    builder.field(root);
    builder.queryName("Wrapper");
    return Optional.of(builder.build()
        .toString());

  }

  private void addFilters(Field root, List<Parameter> parameters, Map<String, Object> inputParams) {
    List<Parameter> filterParams = parameters.stream()
        .filter(p -> p.getExtensions()
            .containsKey("x-dws-filter"))
        .collect(Collectors.toList());
    filterParams.forEach(fp -> {
      Object value = inputParams.get(fp.getName());
      String filterPath = (String) fp.getExtensions()
          .get("x-dws-filter");
      String[] path = filterPath.split("\\.");
      Field targetField = root;
      for (int i = 1; i < path.length - 1; i++) {
        int idx = i;
        targetField = targetField.getChildren()
            .stream()
            .filter(c -> c.getName()
                .equals(path[idx]))
            .findFirst()
            .orElseThrow();
      }
      targetField.setArguments(Map.of(path[path.length - 1], value));
    });
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
