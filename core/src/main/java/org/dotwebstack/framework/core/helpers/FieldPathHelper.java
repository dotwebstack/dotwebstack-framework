package org.dotwebstack.framework.core.helpers;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

public class FieldPathHelper {

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

  public static ObjectField getLeaf(List<ObjectField> fieldPath) {
    return fieldPath.get(fieldPath.size() - 1);
  }

  public static boolean isNestedFieldPath(String path) {
    return path.contains(".");
  }

  public static String getFieldKey(String keyPath) {
    if(isNestedFieldPath(keyPath)){
      var splittedKeys = Arrays.asList(keyPath.split("\\."));
      return splittedKeys.get(splittedKeys.size()-1);
    }
    return keyPath;
  }
}
