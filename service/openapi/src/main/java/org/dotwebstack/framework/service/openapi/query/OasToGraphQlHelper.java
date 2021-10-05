package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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

  public static Optional<Field> toQueryField(@NonNull String queryName, @NonNull String selectionSet) {
    Field rootField = new Field();
    rootField.setName(queryName);
    rootField.setSelectionSet(selectionSet);

    return Optional.of(rootField);
  }

  public static Optional<Field> toQueryField(@NonNull String queryName, @NonNull ResponseTemplate responseTemplate,
      @NonNull Map<String, Object> inputParams, @NonNull List<String> requiredFields, boolean pagingEnabled) {
    var responseObject = responseTemplate.getResponseField();

    if (responseObject == null) {
      return Optional.empty();
    }
    List<OasField> root = findGraphqlObject(responseObject);

    if (root.size() != 1) {
      throw invalidConfigurationException("Expected 1 graphql rootobject but found {}", root.size());
    }

    OasField rootResponseObject = root.get(0);
    Field rootField = new Field();
    List<Field> children = getChildFields("", rootResponseObject, inputParams);
    List<Field> required = getRequiredFields(requiredFields);

    rootField.setChildren(merge(Stream.concat(children.stream(), required.stream())
        .collect(Collectors.toList())));
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

  private static List<Field> getRequiredFields(List<String> requiredFields) {
    return requiredFields.stream()
        .map(requiredField -> {
          List<String> parts = Arrays.stream(requiredField.split("\\."))
              .collect(Collectors.toList());
          Collections.reverse(parts);

          ListIterator<String> iterator = parts.listIterator();
          Field current = null;
          while (iterator.hasNext()) {
            current = createRequiredField(current, iterator.next());
          }
          return current;

        })
        .collect(Collectors.toList());
  }

  private static Field createRequiredField(Field child, String name) {
    Field field = Field.builder()
        .name(name)
        .build();
    if (child != null) {
      field.getChildren()
          .add(child);
    }
    return field;
  }

  private static List<Field> merge(List<Field> fields) {
    List<Field> result = Lists.newArrayList();
    fields.stream()
        .collect(Collectors.groupingBy(Field::getName, TreeMap::new, Collectors.toList()))
        .forEach((key, value) -> result.add(Field.builder()
            .name(key)
            .nodeField(value.stream()
                .anyMatch(Field::isNodeField))
            .collectionNode(value.stream()
                .anyMatch(Field::isCollectionNode))
            .children(merge(value.stream()
                .flatMap(f -> f.getChildren()
                    .stream())
                .collect(Collectors.toList())))
            .build()));
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
