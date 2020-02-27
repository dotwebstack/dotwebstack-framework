package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class ResponseContextHelper {

  private ResponseContextHelper() {}

  public static Set<String> getPathsForSuccessResponse(@NonNull ResponseSchemaContext responseSchemaContext,
      @NonNull Map<String, Object> inputParams) {
    Optional<ResponseTemplate> successResponse = responseSchemaContext.getResponses()
        .stream()
        .filter(template -> template.isApplicable(200, 299))
        .findFirst();

    if (successResponse.isPresent()) {
      ResponseTemplate responseTemplate = successResponse.get();
      Set<String> requiredFields =
          getResponseObject(responseSchemaContext.getGraphQlField(), inputParams, responseTemplate);
      requiredFields.addAll(responseSchemaContext.getRequiredFields());
      return requiredFields;
    } else {
      throw invalidConfigurationException("No success response found for ResponseSchemaContext!");
    }
  }

  private static Set<String> getResponseObject(GraphQlField graphQlField, Map<String, Object> inputParams,
      ResponseTemplate responseTemplate) {
    ResponseObject responseObject = responseTemplate.getResponseObject();

    if (responseObject == null) {
      return Collections.emptySet();
    } else {
      return getRequiredResponseObject("", responseObject, graphQlField, inputParams, true).keySet()
          .stream()
          .map(path -> {
            if (path.startsWith(responseObject.getIdentifier() + ".")) {
              return path.substring(path.indexOf('.') + 1);
            }
            return path;
          })
          .filter(path -> !path.isEmpty())
          .collect(Collectors.toSet());
    }
  }

  static Map<String, SchemaSummary> getRequiredResponseObject(String prefix, ResponseObject responseObject,
      GraphQlField graphQlField, Map<String, Object> inputParams, boolean skipPath) {
    Map<String, SchemaSummary> responseObjects = new HashMap<>();
    StringJoiner joiner = getStringJoiner(prefix);

    SchemaSummary summary = responseObject.getSummary();
    boolean skip = skipPath;
    if (!summary.isEnvelope() && !Objects.equals(summary.getType(), OasConstants.ARRAY_TYPE) && summary.getComposedOf()
        .isEmpty()) {
      if (!skipPath || !Objects.equals(summary.getType(), OasConstants.OBJECT_TYPE)) {
        joiner.add(responseObject.getIdentifier());
        if (summary.isRequired()) {
          responseObjects.put(joiner.toString(), summary);
        }
      }
      skip = false;
    }
    if (summary.isRequired() || summary.isEnvelope()
        || isExpanded(inputParams, getPathString(prefix, responseObject))) {
      handleSubSchemas(graphQlField, inputParams, responseObjects, joiner, responseObject, skip);
    }
    return responseObjects;
  }

  private static GraphQlField getChildFieldByName(ResponseObject responseObject, GraphQlField graphQlField) {
    return graphQlField.getFields()
        .stream()
        .filter(field -> field.getName()
            .equals(responseObject.getIdentifier()))
        .findFirst()
        .orElse(graphQlField);
  }

  private static void handleSubSchemas(GraphQlField graphQlField, Map<String, Object> inputParams,
      Map<String, SchemaSummary> responseObjects, StringJoiner joiner, ResponseObject responseObject, boolean skip) {

    GraphQlField subGraphQlField;
    String joinString = joiner.toString();
    List<ResponseObject> subSchemas;

    SchemaSummary summary = responseObject.getSummary();
    if (!summary.getChildren()
        .isEmpty()) {
      subGraphQlField = getChildFieldByName(responseObject, graphQlField);
      subSchemas = summary.getChildren();
    } else if (!summary.getComposedOf()
        .isEmpty()) {
      subGraphQlField = getChildFieldByName(responseObject, graphQlField);
      joinString = removeLastElementFromPath(joiner);
      subSchemas = summary.getComposedOf();
    } else if (!summary.getItems()
        .isEmpty()) {
      subGraphQlField = graphQlField;
      subSchemas = summary.getItems();
    } else {
      return;
    }

    extractResponseObjects(inputParams, responseObjects, subGraphQlField, skip, subSchemas, joinString);
  }

  private static String removeLastElementFromPath(StringJoiner joiner) {
    String joinString;
    joinString = joiner.toString()
        .contains(".")
            ? joiner.toString()
                .substring(0, joiner.toString()
                    .lastIndexOf('.'))
            : "";
    return joinString;
  }

  private static void extractResponseObjects(Map<String, Object> inputParams,
      Map<String, SchemaSummary> responseObjects, GraphQlField childField, boolean finalSkip,
      List<ResponseObject> children, String prefix) {
    children.stream()
        .flatMap(child -> getRequiredResponseObject(prefix, child, childField, inputParams, finalSkip).entrySet()
            .stream())
        .forEach(entry -> responseObjects.put(entry.getKey(), entry.getValue()));
  }

  public static String getPathString(String prefix, ResponseObject responseObject) {
    StringJoiner expandJoiner = new StringJoiner(".");
    if (!prefix.isBlank()) {
      expandJoiner.add(prefix);
    }
    expandJoiner.add(responseObject.getIdentifier());
    return expandJoiner.toString();
  }

  private static StringJoiner getStringJoiner(String prefix) {
    StringJoiner joiner = new StringJoiner(".");
    if (!prefix.isEmpty()) {
      joiner.add(prefix);
    }
    return joiner;
  }

  @SuppressWarnings("unchecked")
  public static boolean isExpanded(Map<String, Object> inputParams, @NonNull String path) {
    if (Objects.isNull(inputParams)) {
      return false;
    }
    List<String> expandVariables = (List<String>) inputParams.get(X_DWS_EXPANDED_PARAMS);
    if (Objects.nonNull(expandVariables)) {
      return expandVariables.stream()
          .anyMatch(path::equals);
    }
    return false;
  }
}
