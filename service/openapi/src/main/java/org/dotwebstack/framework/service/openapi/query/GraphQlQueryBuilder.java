package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathsForSuccessResponse;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;

public class GraphQlQueryBuilder {

  public Optional<String> toQuery(@NonNull ResponseSchemaContext responseSchemaContext,
      @NonNull Map<String, Object> inputParams) {

    if (responseSchemaContext.getGraphQlField() == null) {
      return Optional.empty();
    }

    Set<String> requiredPaths = getPathsForSuccessResponse(responseSchemaContext, inputParams);

    StringBuilder builder = new StringBuilder();
    StringJoiner joiner = new StringJoiner(",", "{", "}");
    StringJoiner argumentJoiner = new StringJoiner(",");

    Set<String> queriedPaths = new HashSet<>();
    addToQuery(responseSchemaContext.getGraphQlField(), requiredPaths, queriedPaths, joiner, argumentJoiner,
        inputParams, true, "");
    validateRequiredPathsQueried(requiredPaths, queriedPaths);

    builder.append("query Wrapper");
    if (!argumentJoiner.toString()
        .isEmpty()) {
      builder.append("(");
      builder.append(argumentJoiner);
      builder.append(")");
    }
    builder.append(joiner.toString());
    return Optional.of(builder.toString());
  }

  protected void validateRequiredPathsQueried(Set<String> requiredPaths, Set<String> queriedPaths) {
    /*
     * This method checks if the paths that are required from the OAS schema are added to the GraphQL
     * query. This is checked so that we fail fast, a request now always fails if a field is missing
     * and we can build our tests on that assumption
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

  protected void addToQuery(GraphQlField field, Set<String> requiredPaths, Set<String> queriedPaths,
      StringJoiner joiner, StringJoiner headerArgumentJoiner, Map<String, Object> inputParams, boolean isTopLevel,
      String path) {
    StringJoiner argumentJoiner = new StringJoiner(",", "(", ")");
    argumentJoiner.setEmptyValue("");
    if (!field.getArguments()
        .isEmpty() && isTopLevel) {
      field.getArguments()
          .stream()
          .filter(graphQlArgument -> inputParams.containsKey(graphQlArgument.getName()))
          .forEach(graphQlArgument -> {
            argumentJoiner.add(graphQlArgument.getName() + ": $" + graphQlArgument.getName());
            headerArgumentJoiner.add("$" + graphQlArgument.getName() + ": " + getTypeString(graphQlArgument.getType()));
          });
    }

    if ((isTopLevel || requiredPaths.contains(path) || isGraphQlIdentifier(field.getType())
        || isExpanded(inputParams, path))) {
      if (!field.getFields()
          .isEmpty()) {
        StringJoiner childJoiner = new StringJoiner(",", "{", "}");
        field.getFields()
            .forEach(childField -> {
              String childPath = (path.isEmpty() ? "" : path + ".") + childField.getName();
              addToQuery(childField, requiredPaths, queriedPaths, childJoiner, headerArgumentJoiner, inputParams, false,
                  childPath);
            });
        if (!Objects.equals("{}", childJoiner.toString())) {
          queriedPaths.add(path);
          joiner.add(field.getName() + argumentJoiner.toString() + childJoiner.toString());
        }
      } else {
        queriedPaths.add(path);
        joiner.add(field.getName() + argumentJoiner.toString());
      }
    }
  }

  private boolean isGraphQlIdentifier(String type) {
    return type.replaceAll("!", "")
        .replaceAll("\\[", "")
        .replaceAll("]", "")
        .matches("^ID$");
  }
}
