package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.SCALAR_LIST_FILTER_POSTFIX;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.model.ObjectType;

public final class FilterHelper {

  private FilterHelper() {}

  public static String getTypeNameForFilter(Map<String, String> fieldFilterMap, ObjectType<?> objectType,
      String filterName, FilterConfiguration filterConfiguration) {

    var filter = createFilterItem(filterName, filterConfiguration);

    return getTypeNameForFilter(fieldFilterMap, objectType, filter);
  }

  private static String getTypeNameForFilter(Map<String, String> fieldFilterMap, ObjectType<?> objectType,
      FilterItem filterItem) {

    var fieldName = filterItem.getField();

    var objectField = objectType.getField(fieldName);
    var targetType = objectField.getTargetType();

    if (targetType != null) {
      return String.format("%sFilter", objectField.getTargetType()
          .getName());
    } else {
      var type = Optional.ofNullable(filterItem.getType())
          .filter(FilterType.PARTIAL::equals)
          .map(filterType -> FilterConstants.STRING_PARTIAL)
          .orElse(objectField.getType());

      if (objectField.isList()) {
        type = type.concat(SCALAR_LIST_FILTER_POSTFIX);
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
          .field(filterConfiguration.getField())
          .build();
    }

    return FilterItem.builder()
        .field(filterName)
        .build();
  }

  @Builder
  @Getter
  private static class FilterItem {
    private FilterType type;

    @NonNull
    private String field;
  }
}
