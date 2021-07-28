package org.dotwebstack.framework.service.openapi.response;

import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.Field;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;

public class ResponseToQuery {

  private ResponseToQuery() {
  }

  // TODO: create query model at once
  public static List<Field> toFields(@NonNull ResponseSchemaContext responseSchemaContext,
                                     @NonNull ResponseTemplate template, @NonNull Map<String,
      Object> inputParams) {
    List<Field> requiredFields = toFields(inputParams, template);
    //  requiredFields.addAll(responseSchemaContext.getRequiredFields());
    return requiredFields;
  }

  private static List<Field> toFields(Map<String, Object> inputParams,
                                      ResponseTemplate responseTemplate) {
    var responseObject = responseTemplate.getResponseObject();

    if (responseObject == null) {
      return Collections.emptyList();
    }
    return responseObject.getSummary().getChildren().stream().map(c -> toField("", c, inputParams)).filter(Objects::nonNull).collect(Collectors.toList());
  }

  static Field toField(String currentPath, ResponseObject responseObject,
                       Map<String, Object> inputParams) {

    SchemaSummary summary = responseObject.getSummary();
    boolean isExpanded = isExpanded(inputParams, getPathString(currentPath, responseObject));
    if (summary.isRequired() || summary.isTransient() || isExpanded) {
      Field response = new Field();
      response.setName(responseObject.getIdentifier());
      response.setChildren(toField(getPathString(currentPath, responseObject), inputParams, responseObject));
      return response;
    } else {
      return null;
    }
  }

  private static List<Field> toField(String path, Map<String, Object> inputParams,
                                     ResponseObject responseObject) {

    List<ResponseObject> subSchemas;

    SchemaSummary summary = responseObject.getSummary();
    if (!summary.getChildren()
        .isEmpty()) {
      subSchemas = summary.getChildren();
    } else if (!summary.getComposedOf()
        .isEmpty()) {
      subSchemas = summary.getComposedOf();
    } else if (!summary.getItems()
        .isEmpty()) {
      subSchemas = summary.getItems();
    } else {
      return List.of();
    }

    return extractResponseObjects(path, subSchemas, inputParams);
  }

  private static List<Field> extractResponseObjects(String path, List<ResponseObject> children,
                                                    Map<String, Object> inputParams) {
    return children.stream()
        .map(child -> toField(path, child, inputParams)).collect(Collectors.toList());
  }

  public static String getPathString(String prefix, ResponseObject responseObject) {
    var expandJoiner = new StringJoiner(".");
    if (!prefix.isBlank()) {
      expandJoiner.add(prefix);
    }
    expandJoiner.add(responseObject.getIdentifier());
    return expandJoiner.toString();
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

  public static boolean isCollection(ResponseObject responseObject) {
    if (responseObject.getSummary().hasExtension(X_DWS_ENVELOPE)) {
      return isCollection(responseObject.getSummary()
          .getChildren()
          .get(0));
    } else {
      return responseObject.getSummary()
          .getItems() != null;
    }
  }
}
