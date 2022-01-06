package org.dotwebstack.framework.core.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

public class FieldPathHelper {

  public static List<ObjectField> createFieldPath(ObjectType<?> objectType, String path) {
    var current = objectType;
    var fieldPath = new ArrayList<ObjectField>();

    for (var segment : path.split("\\.")) {
      var field = Optional.ofNullable(current)
          .map(o -> o.getField(segment))
          .orElseThrow();

      current = Optional.ofNullable(field.getTargetType())
          .orElse(null);

      fieldPath.add(field);
    }

    return fieldPath;
  }
}
