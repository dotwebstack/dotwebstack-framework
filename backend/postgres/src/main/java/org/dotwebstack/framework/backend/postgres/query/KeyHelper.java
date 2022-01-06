package org.dotwebstack.framework.backend.postgres.query;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;

final class KeyHelper {

  private KeyHelper() {}

  static void addKeyFields(ObjectRequest objectRequest) {
    // for (int index = 0; index < sortCriteria.getFieldPath()
    // .size(); index++) {
    // ObjectField sortField = sortCriteria.getFieldPath()
    // .get(index);
    //
    // if (index == (sortCriteria.getFieldPath()
    // .size() - 1)) {
    // findOrAddScalarField(objectRequest, sortField);
    // } else {
    // ObjectField nextSortField = sortCriteria.getFieldPath()
    // .get(index + 1);
    // objectRequest = findOrAddObjectRequest(objectRequest.getObjectFields(), sortField,
    // nextSortField);
    // }
    // }
  }

  private static ObjectRequest findOrAddObjectRequest(ObjectRequest objectRequest,
      Map<FieldRequest, ObjectRequest> objectFields, String objectField) {

    return objectFields.entrySet()
        .stream()
        .filter(field -> field.getKey()
            .getName()
            .equals(objectField))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseGet(() -> createObjectRequest(objectRequest, objectFields, objectField));
  }

  private static ObjectRequest createObjectRequest(ObjectRequest objectRequest,
      Map<FieldRequest, ObjectRequest> objectFields, String objectField) {

    ObjectRequest tempObjectRequest = ObjectRequest.builder()
        .objectType(objectRequest.getObjectType()
            .getField(objectField)
            .getTargetType())
        .build();
    FieldRequest field = FieldRequest.builder()
        .name(objectField)
        .build();
    objectFields.put(field, tempObjectRequest);
    return tempObjectRequest;
  }

  private static void findOrAddScalarField(ObjectRequest objectRequest, String key) {
    Optional<FieldRequest> scalarField = objectRequest.getScalarFields()
        .stream()
        .filter(field -> field.getName()
            .equals(key))
        .findFirst();

    if (scalarField.isEmpty()) {
      FieldRequest field = FieldRequest.builder()
          .name(key)
          .build();
      objectRequest.getScalarFields()
          .add(field);
    }
  }

  static Map<String, String> parseComposedKeyField(String composedKey) {
    var splittedKey = Arrays.asList(composedKey.split("\\.", 2));
    String objectType = splittedKey.get(0);
    String keyField = splittedKey.get(1);

    if (keyField.contains(".")) {
      parseComposedKeyField(keyField);
    }

    return Map.of("keyField", keyField, "objectType", objectType);
  }
}
