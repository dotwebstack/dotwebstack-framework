package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.oas.OasArrayField;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;
import org.dotwebstack.framework.service.openapi.response.oas.OasObjectField;
import org.dotwebstack.framework.service.openapi.response.oas.OasType;

public class OasToGraphQlHelper {

  private OasToGraphQlHelper() {}

  public static Optional<Field> toQueryField(@NonNull String queryName, @NonNull ResponseTemplate responseTemplate,
      @NonNull Map<String, Object> inputParams, boolean pagingEnabled) {
    var responseObject = responseTemplate.getResponseObject();

    if (responseObject == null) {
      return Optional.empty();
    }
    List<OasField> root = findGraphqlObject(responseObject);

    if (root.size() != 1) {
      throw invalidConfigurationException("Expected 1 graphql rootobject but found {}", root.size());
    }

    OasField rootResponseObject = root.get(0);
    Field rootField = new Field();
    rootField.setChildren(getChildFields("", rootResponseObject, inputParams));
    rootField.setName(queryName);
    rootField.setCollectionNode(rootResponseObject.isArray() && !(((OasArrayField) rootResponseObject).getContent()
        .isScalar()));

    if (pagingEnabled) {
      addPagingNodes(rootField);
    }

    return Optional.of(rootField);
  }

  private static void addPagingNodes(Field field) {
    field.getChildren()
        .forEach(OasToGraphQlHelper::addPagingNodes);
    if (field.isCollectionNode()) {
      Field nodeField = Field.builder()
          .name("nodes")
          .nodeField(true)
          .build();
      nodeField.setChildren(field.getChildren());
      field.setChildren(List.of(nodeField));
    }
  }

  private static List<OasField> findGraphqlObject(OasField field) {
    List<OasField> subSearch;
    if (field.isDefault()) {
      return List.of();
    }
    switch (field.getType()) {
      case OBJECT:
        if (!((OasObjectField) field).isEnvelope()) {
          return List.of(field);
        } else {
          subSearch = new ArrayList<>(((OasObjectField) field).getFields()
              .values());
        }
        break;
      case ARRAY:
      case SCALAR:
        return List.of(field);
      case SCALAR_EXPRESSION:
      case ONE_OF:
      default:
        return List.of();
    }

    return subSearch.stream()
        .map(OasToGraphQlHelper::findGraphqlObject)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static Field toField(String currentPath, String identifier, OasField responseObject,
      Map<String, Object> inputParams) {
    Field result = new Field();
    result.setName(identifier);
    result.setChildren(getChildFields(currentPath, responseObject, inputParams));
    result.setCollectionNode(responseObject.isArray() && !(((OasArrayField) responseObject).getContent()
        .isScalar()));

    return result;
  }

  private static List<Field> getChildFields(String currentPath, OasField field, Map<String, Object> inputParams) {
    if (field.getType() == OasType.ARRAY) {
      return getChildFields(currentPath, ((OasArrayField) field).getContent(), inputParams);
    } else if (field.getType() == OasType.OBJECT) {

      Stream<Map.Entry<String, OasField>> filteredChildren = ((OasObjectField) field).getFields()
          .entrySet()
          .stream()
          .filter(e -> shouldAdd(e.getValue(), e.getKey(), inputParams, currentPath));
      return filteredChildren.flatMap(e -> {
        String identifier = e.getKey();
        OasField childField = e.getValue();
        List<OasField> fields = findGraphqlObject(childField);
        return fields.stream()
            .map(cc -> toField(getPathString(currentPath, identifier), identifier, cc, inputParams));
      })
          .collect(Collectors.toList());
    }
    return List.of();

  }

  private static boolean shouldAdd(OasField field, String identifier, Map<String, Object> inputParams,
      String currentPath) {
    boolean isExpanded = isExpanded(inputParams, getPathString(currentPath, identifier));
    return field.isRequired() || isExpanded;
  }

}
