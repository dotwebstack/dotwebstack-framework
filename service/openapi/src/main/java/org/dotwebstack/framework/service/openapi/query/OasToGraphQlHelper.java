package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_DEFAULT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;

public class OasToGraphQlHelper {

  private OasToGraphQlHelper() {}

  public static Optional<Field> toQueryField(@NonNull String queryName, @NonNull ResponseTemplate responseTemplate,
      @NonNull Map<String, Object> inputParams, boolean pagingEnabled) {
    var responseObject = responseTemplate.getResponseObject();

    if (responseObject == null) {
      return Optional.empty();
    }
    List<ResponseObject> root = findGraphqlObject(responseObject);

    if (root.size() != 1) {
      throw invalidConfigurationException("Expected 1 graphql rootobject for object {} but found {}",
          responseObject.getIdentifier(), root.size());
    }

    ResponseObject rootResponseObject = root.get(0);
    Field rootField = new Field();
    rootField.setChildren(getChildFields("", rootResponseObject, inputParams));
    rootField.setName(queryName);
    rootField.setCollectionNode(rootResponseObject.isArray() && !rootResponseObject.isScalar());

    if (pagingEnabled) {
      addPagingNodes(rootField);
    }

    return Optional.of(rootField);
  }

  public static void addPagingNodes(GraphQlQuery query) {
    addPagingNodes(query.getField());
  }

  private static void addPagingNodes(Field field) {
    if (field.isCollectionNode()) {
      field.getChildren()
          .forEach(OasToGraphQlHelper::addPagingNodes);
      Field nodeField = Field.builder()
          .name("nodes")
          .nodeField(true)
          .build();
      nodeField.setChildren(field.getChildren());
      field.setChildren(List.of(nodeField));
    }
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
    result.setChildren(getChildFields(currentPath, responseObject, inputParams));
    result.setCollectionNode(responseObject.isArray() && !responseObject.isScalar());

    return result;
  }

  private static List<Field> getChildFields(String currentPath, ResponseObject responseObject,
      Map<String, Object> inputParams) {
    Stream<ResponseObject> childResponseObjects;
    if (responseObject.isComposedOf()) {
      childResponseObjects = getComposedObjectChildren(responseObject);
    } else {
      childResponseObjects = responseObject.getSummary()
          .getChildren()
          .stream();
    }

    return childResponseObjects.filter(c -> shouldAdd(c, inputParams, currentPath))
        .map(OasToGraphQlHelper::findGraphqlObject)
        .flatMap(List::stream)
        .map(cc -> toField(getPathString(currentPath, responseObject), cc, inputParams))
        .collect(Collectors.toList());
  }

  private static Stream<ResponseObject> getComposedObjectChildren(ResponseObject responseObject) {
    return responseObject.getSummary()
        .getComposedOf()
        .stream()
        .map(r -> r.getSummary()
            .getChildren())
        .flatMap(List::stream);
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
