package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeString;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.response.GraphQlBinding;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseToQuery;

public class GraphQlQueryBuilder {

  public Optional<String> toQuery(@NonNull ResponseSchemaContext responseSchemaContext,
      @NonNull Map<String, Object> inputParams) {

    GraphQlBinding graphQlBinding = responseSchemaContext.getGraphQlBinding();
    String queryName = graphQlBinding.getQueryName();

    if(queryName == null || queryName.isEmpty()){
      return Optional.empty();
    }

    ResponseTemplate okResponse = responseSchemaContext.getResponses()
        .stream()
        .filter(r -> r.getResponseCode() == 200)
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException("No OK response found"));

    List<Field> fields = ResponseToQuery.toFields(responseSchemaContext, okResponse, inputParams);
    if (isCollection(graphQlBinding)) {
      return toCollectionQuery(okResponse, queryName, fields);
    } else {
      String selectorName = graphQlBinding.getSelector();
      String selectorValue = inputParams.get(selectorName).toString();
      return toResourceQuery(queryName, fields, selectorName, selectorValue);
    }
  }

  protected boolean isCollection(GraphQlBinding binding) {
    return binding.getSelector() == null;
  }

  private Optional<String> toCollectionQuery(ResponseTemplate responseTemplate, String queryName, List<Field> fields) {
    ResourceQuery.ResourceQueryBuilder builder = ResourceQuery.builder();

    Field collectionField = new Field(queryName, null, null);
    Field nodesField = new Field("nodes", null, null);
    nodesField.setChildren(fields);

    collectionField.setChildren(List.of(nodesField));
    builder.field(collectionField);

    return Optional.of(builder.build()
        .toString());
  }

  private Optional<String> toResourceQuery(String queryName, List<Field> nodes, String selectorName, String selectorValue) {
    ResourceQuery.ResourceQueryBuilder builder = ResourceQuery.builder();
    Field root = new Field();
    root.setChildren(nodes);
    root.setName(queryName);
    root.setArguments(Map.of(selectorName, selectorValue));
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

  public static Field toField(@NonNull ResponseTemplate responseTemplate, Set<String> paths) {
    ResponseObject responseObject = responseTemplate.getResponseObject();
    return toField(responseObject, paths, "").get(0);
  }

  private static List<Field> toField(ResponseObject responseObject, Set<String> paths, String currentPath) {
    if (responseObject.getSummary().hasExtension(X_DWS_ENVELOPE)){
      return envelopeToField(responseObject, paths, currentPath);
    } else if ("array".equals(responseObject.getSummary()
        .getSchema()
        .getType())) {
      return arrayToField(responseObject, paths, currentPath);
    } else {
      return nonEnvelopeToField(responseObject, paths, currentPath);
    }
  }

  private static List<Field> arrayToField(ResponseObject responseObject, Set<String> paths, String currentPath) {
    ResponseObject item = responseObject.getSummary()
        .getItems()
        .get(0);
    return toField(item, paths, "");
  }

  private static List<Field> nonEnvelopeToField(ResponseObject responseObject, Set<String> paths, String currentPath) {
    String identifier = responseObject.getIdentifier();
    String newPath = currentPath + "." + identifier;
    Field result = Field.builder()
        .name(identifier)
        .build();
    result.setChildren(responseObject.getSummary()
        .getChildren()
        .stream()
        .filter(mapToQuery())
        .flatMap(child -> toField(child, paths, newPath).stream())
        .collect(Collectors.toList()));
    return List.of(result);
  }

  private static List<Field> envelopeToField(ResponseObject responseObject, Set<String> paths, String currentPath) {
    return responseObject.getSummary()
        .getChildren()
        .stream()
        .filter(mapToQuery())
        .flatMap(child -> toField(child, paths, currentPath).stream())
        .collect(Collectors.toList());
  }

  private static Predicate<ResponseObject> mapToQuery() {
    return c -> c.getSummary()
        .isRequired()
        && c.getSummary()
            .getDwsExpr() == null;
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

  protected void addToQuery(GraphQlField field, Set<String> requiredPaths, Set<String> queriedPaths,
      StringJoiner joiner, StringJoiner headerArgumentJoiner, Map<String, Object> inputParams, boolean isTopLevel,
      String path) {
    var argumentJoiner = new StringJoiner(",", "(", ")");
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
        var childJoiner = new StringJoiner(",", "{", "}");
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
    return type.replace("!", "")
        .replace("\\[", "")
        .replace("]", "")
        .matches("^ID$");
  }
}
