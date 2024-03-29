package org.dotwebstack.framework.core.helpers;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

public class FieldPathHelper {

  private static final String REF = "ref";

  private FieldPathHelper() {}

  public static List<ObjectField> createFieldPath(ObjectType<?> objectType, String path) {
    var current = objectType;
    var fieldPath = new ArrayList<ObjectField>();

    for (var segment : path.split("\\.")) {
      var field = ofNullable(current).map(o -> o.getField(segment))
          .orElseThrow();

      current = ofNullable(field.getTargetType()).orElse(null);

      fieldPath.add(field);
    }

    return fieldPath;
  }

  public static Optional<ObjectField> getParentOfRefField(List<ObjectField> fieldPath) {
    var numberOfFields = fieldPath.size();
    for (var i = 0; i < numberOfFields; i++) {
      var current = fieldPath.get(i);
      Optional<ObjectField> previous = i > 0 ? Optional.of(fieldPath.get(i - 1)) : Optional.empty();
      if (current.getName()
          .equals(REF)) {
        return previous;
      }
    }
    return Optional.empty();
  }

  public static ObjectField getLeaf(List<ObjectField> fieldPath) {
    return fieldPath.get(fieldPath.size() - 1);
  }

  public static boolean isNestedFieldPath(String path) {
    return path.contains(".");
  }

  public static boolean isNested(List<ObjectField> fieldPath) {
    return getLeaf(fieldPath).getObjectType()
        .isNested();
  }

  public static String getFieldKey(String keyPath) {
    var splittedKeys = Arrays.asList(keyPath.split("\\."));
    return splittedKeys.get(splittedKeys.size() - 1);
  }

  public static boolean fieldPathContainsRef(List<ObjectField> fieldPaths) {
    return fieldPaths.stream()
        .anyMatch(fieldPath -> fieldPath.getName()
            .equals(REF));
  }

  @SuppressWarnings("squid:S1452")
  public static ObjectType<? extends ObjectField> getObjectType(ObjectType<?> objectType, String keyPath) {
    var fieldPath = createFieldPath(objectType, keyPath);
    if (fieldPath.size() > 1) {
      return fieldPath.get(fieldPath.size() - 2)
          .getTargetType();
    }
    return objectType;
  }
}
