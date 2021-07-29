package org.dotwebstack.framework.service.openapi.query;

import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;

public class OasToGraphQlHelper {

  private OasToGraphQlHelper() {
  }

  public static List<Field> toFields(@NonNull ResponseTemplate responseTemplate, @NonNull Map<String, Object> inputParams) {
    var responseObject = responseTemplate.getResponseObject();

    if (responseObject == null) {
      return Collections.emptyList();
    }
    List<ResponseObject> root = findGraphqlObject(responseObject);

    if(root.size()!=1){
      throw new InvalidConfigurationException(format("Expected 1 graphql rootobject for object %s but found %s", responseObject.getIdentifier(), root.size()));
    }
    return root.get(0).getSummary().getChildren().stream().map(OasToGraphQlHelper::findGraphqlObject).flatMap(List::stream).map(c -> toField("", c, inputParams)).filter(Objects::nonNull).collect(Collectors.toList());
  }

  private static List<ResponseObject> findGraphqlObject(ResponseObject responseObject) {
    SchemaSummary summary = responseObject.getSummary();
    if (isEnvelope(responseObject) && !summary.getChildren()
        .isEmpty()) { // envelope
      return summary.getChildren().stream().map(OasToGraphQlHelper::findGraphqlObject).flatMap(List::stream).collect(Collectors.toList());
    } else if (!summary.getComposedOf()
        .isEmpty()) { // composed
      return summary.getComposedOf().stream().map(OasToGraphQlHelper::findGraphqlObject).flatMap(List::stream).collect(Collectors.toList());
    } else if (!summary.getItems()
        .isEmpty()) { // list
      return summary.getItems().stream().map(OasToGraphQlHelper::findGraphqlObject).flatMap(List::stream).collect(Collectors.toList());
    } else if (!isEnvelope(responseObject) && !isExpression(responseObject)) {
      return List.of(responseObject);
    } else {
      return List.of();
    }
  }

  private static boolean isExpression(ResponseObject responseObject) {
    return responseObject.getSummary().getDwsExpr() != null;
  }

  private static boolean isEnvelope(ResponseObject responseObject) {
    return responseObject.getSummary().hasExtension(X_DWS_ENVELOPE);
  }

  static Field toField(String currentPath, ResponseObject responseObject,
                       Map<String, Object> inputParams) {

    SchemaSummary summary = responseObject.getSummary();
    boolean isExpanded = isExpanded(inputParams, getPathString(currentPath, responseObject));
    boolean shouldAdd = summary.isRequired() || summary.isTransient() || isExpanded;
    if (shouldAdd) {
      Field response = new Field();
      response.setName(responseObject.getIdentifier());
      List<Field> children =
          responseObject.getSummary().getChildren().stream().map(OasToGraphQlHelper::findGraphqlObject
      ).flatMap(List::stream).map(cc -> toField(getPathString(currentPath, responseObject), cc, inputParams)).filter(Objects::nonNull).collect(Collectors.toList());
      response.setChildren(children);
      return response;
    } else {
      return null;
    }
  }

}
