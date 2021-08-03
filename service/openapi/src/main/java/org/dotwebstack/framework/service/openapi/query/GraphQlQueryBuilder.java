package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
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

    List<Field> fields = OasToGraphQlHelper.toQueryFields(okResponse, inputParams);
    Optional<GraphQlQuery> query = toQuery(queryName, fields);
    query.ifPresent(q -> addFilters(q, responseSchemaContext.getParameters(), inputParams));

    return query.map(GraphQlQuery::toString);
  }

  private Set<Select> getSelects(List<Parameter> parameters, Map<String, Object> inputParams){
    return parameters.stream().map(p -> {
      String name = p.getName();
      if (p.getExtensions() != null) {
        String select = (String) p.getExtensions().get("x-dws-select");
        if (select != null && inputParams.get(name) != null) {
            return new Select.SelectBuilder().fieldPath(select).value(inputParams.get(name)).build();
        }
      }
      return null;
    }).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  private void addFilters(GraphQlQuery query, List<Parameter> parameters, Map<String, Object> inputParams) {
    Set<Select> selects = getSelects(parameters, inputParams);

    selects.forEach(select -> {
          String[] path = select.getFieldPath().split("\\.");
          Field field = query.getField();
          for (int i = 0; i < path.length - 1; i++) {
            int finalI = i;
            field =
                field.getChildren().stream().filter(f -> f.getName().equals(path[finalI])).findFirst().orElseThrow();
          }
          field.getArguments().put(path[path.length-1], select.getValue());
    });
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
