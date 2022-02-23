package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectFieldHelper.createSystemAlias;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;

public class ObjectRequestHelper {

  private ObjectRequestHelper() {}

  public static void addSortFields(CollectionRequest collectionRequest) {
    collectionRequest.getSortCriterias()
        .forEach(sortCriteria -> addSortFields(collectionRequest, sortCriteria));
  }

  private static void addSortFields(CollectionRequest collectionRequest, SortCriteria sortCriteria) {
    ObjectRequest objectRequest = collectionRequest.getObjectRequest();

    for (int index = 0; index < sortCriteria.getFieldPath()
        .size(); index++) {
      ObjectField sortField = sortCriteria.getFieldPath()
          .get(index);

      if (index == (sortCriteria.getFieldPath()
          .size() - 1)) {
        findOrAddScalarField(objectRequest, sortField);
      } else {
        ObjectField nextSortField = sortCriteria.getFieldPath()
            .get(index + 1);
        objectRequest = findOrAddObjectRequest(objectRequest.getObjectFields(), sortField, nextSortField);
      }
    }
  }

  public static void addKeyFields(final ObjectRequest objectRequest) {
    var keyCriterias = objectRequest.getKeyCriterias();

    keyCriterias.forEach(keyCriteria -> {
      final AtomicReference<ObjectRequest> current = new AtomicReference<>(objectRequest);

      var fieldPath = keyCriteria.getFieldPath();

      for (int index = 0; index < fieldPath.size(); index++) {
        ObjectField keyField = fieldPath.get(index);
        if (index == (fieldPath.size() - 1)) {
          findOrAddScalarField(current.get(), keyField);
        } else {
          ObjectField nextKeyField = fieldPath.get(index + 1);
          current.set(findOrAddObjectRequest(current.get()
              .getObjectFields(), keyField, nextKeyField));
        }
      }
    });
  }

  private static ObjectRequest findOrAddObjectRequest(Map<FieldRequest, ObjectRequest> objectFields,
      ObjectField objectField, ObjectField nextObjectField) {
    return objectFields.entrySet()
        .stream()
        .filter(field -> {
          // Only reuse existing ObjectRequest when aliases are not used.
          var resultKey = field.getKey()
              .getResultKey();
          return resultKey.equals(objectField.getName()) || createSystemAlias(objectField).equals(resultKey);
        })
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseGet(() -> createObjectRequest(objectFields, objectField, nextObjectField));
  }

  private static ObjectRequest createObjectRequest(Map<FieldRequest, ObjectRequest> objectFields,
      ObjectField objectField, ObjectField nextObjectField) {
    var objectRequest = ObjectRequest.builder()
        .objectType(nextObjectField.getObjectType())
        .build();

    var field = FieldRequest.builder()
        .name(objectField.getName())
        .resultKey(createSystemAlias(objectField))
        .build();
    objectFields.put(field, objectRequest);
    return objectRequest;
  }

  private static void findOrAddScalarField(ObjectRequest objectRequest, ObjectField objectField) {
    Optional<FieldRequest> scalarField = objectRequest.getScalarFields()
        .stream()
        .filter(field -> field.getName()
            .equals(objectField.getName()))
        .findFirst();

    if (scalarField.isEmpty()) {
      var field = FieldRequest.builder()
          .name(objectField.getName())
          .build();
      objectRequest.getScalarFields()
          .add(field);
    }
  }
}
