package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;

public final class FilterHelper {

  private FilterHelper() {}

  public static String getTypeNameForFilter(Schema schema, Map<String, String> fieldFilterMap, ObjectType<?> objectType,
      String filterName, FilterConfiguration filterConfiguration) {
    String fieldPath;
    if (filterConfiguration.getField() != null) {
      fieldPath = filterConfiguration.getField();
    } else {
      fieldPath = filterName;
    }

    return getTypeNameForFilter(schema, fieldFilterMap, objectType, fieldPath, filterName);
  }

  private static String getTypeNameForFilter(Schema schema, Map<String, String> fieldFilterMap,
      ObjectType<?> objectType, String fieldPath, String filterName) {
    var nested = fieldPath.contains(".");

    var fieldName = StringUtils.substringBefore(fieldPath, ".");

    var objectField = objectType.getField(fieldName);

    if (nested) {
      var nestedObjectType = schema.getObjectType(objectField.getType())
          .orElseThrow();
      var rest = StringUtils.substringAfter(fieldPath, ".");

      return getTypeNameForFilter(schema, fieldFilterMap, nestedObjectType, rest, filterName);

    } else {
      var type = objectField.getType();

      return getTypeNameForFilter(fieldFilterMap, type);
    }
  }

  public static String getTypeNameForFilter(Map<String, String> fieldFilterMap, String typeName) {
    if (fieldFilterMap.containsKey(typeName)) {
      return fieldFilterMap.get(typeName);
    }

    throw illegalArgumentException("Type name '{}' has no corresponding filter.", typeName);
  }
}
