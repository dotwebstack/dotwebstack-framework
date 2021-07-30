package org.dotwebstack.framework.service.openapi.query;

import static java.lang.String.format;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_DEFAULT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;

public class OasToGraphQlHelper {

  private OasToGraphQlHelper() {}

  public static List<Field> toQueryFields(@NonNull ResponseTemplate responseTemplate,
      @NonNull Map<String, Object> inputParams) {
    var responseObject = responseTemplate.getResponseObject();

    if (responseObject == null) {
      return Collections.emptyList();
    }
    List<ResponseObject> root = findGraphqlObject(responseObject);

    if (root.size() != 1) {
      throw new InvalidConfigurationException(format("Expected 1 graphql rootobject for object %s but found %s",
          responseObject.getIdentifier(), root.size()));
    }
    return root.get(0)
        .getSummary()
        .getChildren()
        .stream()
        .filter(c -> shouldAdd(c, inputParams, ""))
        .map(OasToGraphQlHelper::findGraphqlObject)
        .flatMap(List::stream)
        .map(c -> toField("", c, inputParams))
        .collect(Collectors.toList());
  }

  private static List<ResponseObject> findGraphqlObject(ResponseObject responseObject) {
    SchemaSummary summary = responseObject.getSummary();
    if (isExpression(responseObject) || isDefault(responseObject)) {
      return List.of();
    }
    List<ResponseObject> subSearch = List.of();
    if (isEnvelope(responseObject) && !summary.getChildren()
        .isEmpty()) { // envelope
      subSearch = summary.getChildren();
    } else if (!summary.getComposedOf()
        .isEmpty()) { // composed
      subSearch = summary.getComposedOf();
    } else if (!summary.getItems()
        .isEmpty()) { // list
      subSearch = summary.getItems();
    } else if (!isEnvelope(responseObject)) {
      return List.of(responseObject);
    }
    return subSearch.stream()
        .map(OasToGraphQlHelper::findGraphqlObject)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static Field toField(String currentPath, ResponseObject responseObject, Map<String, Object> inputParams) {

    Field result = new Field();
    result.setName(responseObject.getIdentifier());
    List<Field> children = responseObject.getSummary()
        .getChildren()
        .stream()
        .filter(c -> shouldAdd(c, inputParams, currentPath))
        .map(OasToGraphQlHelper::findGraphqlObject)
        .flatMap(List::stream)
        .map(cc -> toField(getPathString(currentPath, responseObject), cc, inputParams))
        .collect(Collectors.toList());
    result.setChildren(children);
    return result;
  }

  private static boolean shouldAdd(ResponseObject responseObject, Map<String, Object> inputParams, String currentPath) {
    SchemaSummary summary = responseObject.getSummary();
    boolean isExpanded = isExpanded(inputParams, getPathString(currentPath, responseObject));
    return summary.isRequired() || summary.isTransient() || isExpanded;
  }

  private static boolean isDefault(ResponseObject responseObject) {
    return responseObject.getSummary()
        .hasExtension(X_DWS_DEFAULT);
  }

  private static boolean isExpression(ResponseObject responseObject) {
    return responseObject.getSummary()
        .getDwsExpr() != null;
  }

  private static boolean isEnvelope(ResponseObject responseObject) {
    return responseObject.getSummary()
        .hasExtension(X_DWS_ENVELOPE);
  }

}
