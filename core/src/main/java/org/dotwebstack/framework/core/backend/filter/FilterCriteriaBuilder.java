package org.dotwebstack.framework.core.backend.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.MapHelper.resolveSuppliers;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.helpers.MapHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

@Setter
@Accessors(fluent = true)
public class FilterCriteriaBuilder {

  private ObjectType<?> objectType;

  private Map<String, Object> argument;

  private List<ObjectField> fieldPath = new ArrayList<>();

  private int currentDepth = 0;

  private int maxDepth;

  private FilterCriteriaBuilder() {}

  public static FilterCriteriaBuilder newFilterCriteriaBuilder() {
    return new FilterCriteriaBuilder();
  }

  public FilterCriteria build() {
    if (currentDepth > maxDepth) {
      throw unsupportedOperationException("Max depth of '{}' is exceeded for filter path '{}'", maxDepth,
          fieldPath.stream()
              .map(ObjectField::getName)
              .collect(Collectors.joining(".")));
    }

    var andCriterias = argument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(argument.get(filterName)))
        .filter(filterName -> !filterName.equals(FilterConstants.OR_FIELD))
        .map(this::build)
        .map(FilterCriteria.class::cast)
        .collect(Collectors.toList());

    FilterCriteria andGroup = GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(andCriterias)
        .build();

    var orGroup = argument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(argument.get(filterName)))
        .filter(filterName -> Objects.equals(filterName, FilterConstants.OR_FIELD))
        .findFirst()
        .map(filterName -> newFilterCriteriaBuilder().objectType(objectType)
            .argument(MapHelper.getNestedMap(argument, filterName))
            .fieldPath(fieldPath)
            .currentDepth(currentDepth)
            .maxDepth(maxDepth)
            .build());

    return orGroup.map(groupFilterCriteria -> (FilterCriteria) GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.OR)
        .filterCriterias(List.of(andGroup, groupFilterCriteria))
        .build())
        .orElse(andGroup);
  }

  private FilterCriteria build(String filterName) {
    if (FilterConstants.EXISTS_FIELD.equalsIgnoreCase(filterName)) {
      if (fieldPath.size() == 0) {
        throw unsupportedOperationException("Filter operator '_exists' is only supported for nested objects");
      }
      return ScalarFieldFilterCriteria.builder()
          .filterType(FilterType.EXACT)
          .fieldPath(fieldPath)
          .value(Map.of(filterName, argument.get(filterName)))
          .build();
    }

    var filterConfiguration = objectType.getFilters()
        .get(filterName);

    var field = objectType.getField(filterConfiguration.getField());

    var targetType = field.getTargetType();

    if (targetType != null) {
      return newFilterCriteriaBuilder().objectType(targetType)
          .argument(MapHelper.getNestedMap(argument, filterName))
          .fieldPath(createFieldPath(field))
          .currentDepth(currentDepth + 1)
          .maxDepth(maxDepth)
          .build();
    }

    var filterValue = buildValue(argument.get(filterName));

    return ScalarFieldFilterCriteria.builder()
        .filterType(filterConfiguration.getType())
        .isCaseSensitive(filterConfiguration.isCaseSensitive())
        .fieldPath(createFieldPath(field))
        .value(filterValue)
        .build();
  }

  private ArrayList<ObjectField> createFieldPath(ObjectField field) {
    var result = new ArrayList<>(fieldPath);
    result.add(field);
    return result;
  }

  private Map<String, Object> buildValue(Object rawValue) {
    if (rawValue instanceof Boolean) {
      return Map.of(FilterConstants.EQ_FIELD, rawValue);
    }

    if (rawValue instanceof Map) {
      return resolveSuppliers(castToMap(rawValue));
    }

    throw illegalArgumentException("Expected entry value of type 'java.util.Map' but got '{}'", rawValue.getClass()
        .getName());
  }

}
