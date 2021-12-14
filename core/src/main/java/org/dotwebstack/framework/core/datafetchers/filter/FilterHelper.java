package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;

public final class FilterHelper {

  private FilterHelper() {}

  public static String getTypeNameForFilter(Schema schema, Map<String, String> fieldFilterMap, ObjectType<?> objectType,
      String filterName, FilterConfiguration filterConfiguration) {

    var filter = createFilterItem(filterName, filterConfiguration);

    return getTypeNameForFilter(schema, fieldFilterMap, objectType, filter);
  }

  private static String getTypeNameForFilter(Schema schema, Map<String, String> fieldFilterMap,
      ObjectType<?> objectType, FilterItem filterItem) {
    var nested = filterItem.getFieldPath()
        .contains(".");

    var fieldName = StringUtils.substringBefore(filterItem.getFieldPath(), ".");

    var objectField = objectType.getField(fieldName);

    if (nested) {
      var nestedObjectType = schema.getObjectType(objectField.getType())
          .orElseThrow();
      var nestedFilter = FilterItem.builder()
          .type(filterItem.getType())
          .fieldPath(StringUtils.substringAfter(filterItem.getFieldPath(), "."))
          .build();

      return getTypeNameForFilter(schema, fieldFilterMap, nestedObjectType, nestedFilter);

    } else {
      var type = Optional.ofNullable(filterItem.getType())
          .filter(FilterType.TERM::equals)
          .map(filterType -> FilterConstants.TERM_NAME)
          .orElse(objectField.getType());

      // TODO ahu: generieker maken
      if (objectField.isList()) {
        type = type.concat("List");
      }
      return getTypeNameForFilter(fieldFilterMap, type);
    }
  }

  public static String getTypeNameForFilter(Map<String, String> fieldFilterMap, String typeName) {
    if (fieldFilterMap.containsKey(typeName)) {
      return fieldFilterMap.get(typeName);
    }

    throw illegalArgumentException("Type name '{}' has no corresponding filter.", typeName);
  }

  private static FilterItem createFilterItem(String filterName, FilterConfiguration filterConfiguration) {
    if (filterConfiguration.getField() != null) {
      return FilterItem.builder()
          .type(filterConfiguration.getType())
          .fieldPath(filterConfiguration.getField())
          .build();
    }

    return FilterItem.builder()
        .fieldPath(filterName)
        .build();
  }

  @Builder
  @Getter
  private static class FilterItem {
    private FilterType type;

    @NonNull
    private String fieldPath;
  }
}
