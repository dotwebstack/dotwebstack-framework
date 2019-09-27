package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class ResponseContextHelper {

  private ResponseContextHelper() {}

  public static Set<String> getRequiredResponseObjectsForSuccessResponse(
      @NonNull ResponseSchemaContext responseSchemaContext, @NonNull Map<String, Object> inputParams) {
    ResponseTemplate successResponse = responseSchemaContext.getResponses()
        .stream()
        .filter(template -> template.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("No success response found for ResponseSchemaContext!"));

    return getRequiredResponseObject("", successResponse.getResponseObject(), responseSchemaContext.getGraphQlField(),
        inputParams, true).keySet()
            .stream()
            .map(path -> path.replaceFirst("^" + successResponse.getResponseObject()
                .getIdentifier() + ".?", ""))
            .filter(path -> !path.isEmpty())
            .collect(Collectors.toSet());
  }

  private static Map<String, SchemaSummary> getRequiredResponseObject(String prefix, ResponseObject responseObject,
      GraphQlField graphQlField, Map<String, Object> inputParams, boolean skipPath) {
    Map<String, SchemaSummary> responseObjects = new HashMap<>();
    StringJoiner joiner = getStringJoiner(prefix);

    GraphQlField childField = graphQlField.getFields()
        .stream()
        .filter(field -> field.getName()
            .equals(responseObject.getIdentifier()))
        .findFirst()
        .orElse(graphQlField);


    SchemaSummary summary = responseObject.getSummary();
    boolean skip = skipPath;
    if (!summary.isEnvelope() && !Objects.equals(summary.getType(), OasConstants.ARRAY_TYPE)) {
      if (!skipPath || !Objects.equals(summary.getType(), OasConstants.OBJECT_TYPE)) {
        joiner.add(responseObject.getIdentifier());
        if (summary.isRequired()) {
          responseObjects.put(joiner.toString(), summary);
        }
      }
      skip = false;
    }

    final boolean finalSkip = skip;
    if (summary.isRequired() || summary.isEnvelope()
        || isExpanded(inputParams, getPathString(prefix, responseObject))) {
      if (!summary.getChildren()
          .isEmpty()) {
        summary.getChildren()
            .stream()
            .flatMap(child -> getRequiredResponseObject(joiner.toString(), child, childField, inputParams, finalSkip)
                .entrySet()
                .stream())
            .forEach(entry -> responseObjects.put(entry.getKey(), entry.getValue()));
      }

      if (!summary.getItems()
          .isEmpty()) {
        summary.getItems()
            .stream()
            .flatMap(item -> getRequiredResponseObject(joiner.toString(), item, graphQlField, inputParams, finalSkip)
                .entrySet()
                .stream())
            .forEach(entry -> responseObjects.put(entry.getKey(), entry.getValue()));
      }
    }
    return responseObjects;
  }

  private static String getPathString(String prefix, ResponseObject responseObject) {
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
  public static boolean isExpanded(@NonNull Map<String, Object> inputParams, @NonNull String path) {
    List<String> expandVariables = (List<String>) inputParams.get(X_DWS_EXPANDED_PARAMS);
    if (Objects.nonNull(expandVariables)) {
      return expandVariables.stream()
          .anyMatch(path::equals);
    }
    return false;
  }
}
