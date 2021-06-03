package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.Map;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

public final class FilterHelper {

  private FilterHelper() {}

  public static String getTypeNameForFilter(Map<String, String> fieldFilterMap, TypeConfiguration<?> typeConfiguration,
      String filterName, FilterConfiguration filterConfiguration) {
    String fieldName;

    if (filterConfiguration.getField() != null) {
      fieldName = filterConfiguration.getField();
    } else {
      fieldName = filterName;
    }

    var typeConfigurationForField = typeConfiguration.getField(fieldName);
    if (typeConfigurationForField.isEmpty()) {
      throw invalidConfigurationException("Filter '{}' doesn't match existing field!", filterName);
    }

    var type = typeConfigurationForField.get().getType());

    return getTypeNameForFilter(fieldFilterMap, type);
  }

  public static String getTypeNameForFilter(Map<String, String> fieldFilterMap, String typeName) {
    if (fieldFilterMap.containsKey(typeName)) {
      return fieldFilterMap.get(typeName);
    }

    throw illegalArgumentException("Type name '{}' has no corresponding filter.", typeName);
  }
}
