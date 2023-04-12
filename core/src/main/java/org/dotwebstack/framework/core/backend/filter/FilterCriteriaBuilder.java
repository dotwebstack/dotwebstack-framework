package org.dotwebstack.framework.core.backend.filter;

import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.EXISTS_FIELD;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.MapHelper.resolveSuppliers;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

@Setter
@Accessors(fluent = true)
public class FilterCriteriaBuilder {

  @NotNull
  private ObjectType<?> objectType;

  @NotNull
  private Map<String, Object> argument;

  private List<ObjectField> fieldPath = new ArrayList<>();

  private int currentDepth = 0;

  private int maxDepth;

  private FilterCriteriaBuilder() {}

  public static FilterCriteriaBuilder newFilterCriteriaBuilder() {
    return new FilterCriteriaBuilder();
  }

  public FilterCriteria build() {
    checkDepth();

    var andGroup = buildAndGroup();

    return buildOrGroup(andGroup).orElse(andGroup);
  }

  private FilterCriteria build(String filterName) {
    if (EXISTS_FIELD.equalsIgnoreCase(filterName)) {
      if (fieldPath.isEmpty()) {
        throw requestValidationException("Filter operator '_exists' is only supported for nested objects");
      }
      return ObjectFieldFilterCriteria.builder()
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
          .argument(getNestedMap(argument, filterName))
          .fieldPath(buildFieldPath(field))
          .currentDepth(currentDepth + 1)
          .maxDepth(maxDepth)
          .build();
    }

    var filterValue = buildValue(argument.get(filterName));

    return ObjectFieldFilterCriteria.builder()
        .filterType(filterConfiguration.getType())
        .isCaseSensitive(filterConfiguration.isCaseSensitive())
        .fieldPath(buildFieldPath(field))
        .value(filterValue)
        .build();
  }

  private Optional<FilterCriteria> buildOrGroup(FilterCriteria andGroup) {
    var orGroup = getArgumentKeys().filter(filterName -> Objects.equals(filterName, FilterConstants.OR_FIELD))
        .findFirst()
        .map(filterName -> newFilterCriteriaBuilder().objectType(objectType)
            .argument(getNestedMap(argument, filterName))
            .fieldPath(fieldPath)
            .currentDepth(currentDepth)
            .maxDepth(maxDepth)
            .build());

    return orGroup.map(filterCriteria -> GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.OR)
        .filterCriterias(List.of(andGroup, filterCriteria))
        .build());
  }

  private FilterCriteria buildAndGroup() {
    var criteria = getArgumentKeys().filter(filterName -> !filterName.equals(FilterConstants.OR_FIELD))
        .map(this::build)
        .map(FilterCriteria.class::cast)
        .toList();

    return GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(criteria)
        .build();
  }

  private List<ObjectField> buildFieldPath(ObjectField field) {
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

    throw requestValidationException("Expected entry value of type 'java.util.Map' but got '{}'", rawValue.getClass()
        .getName());
  }

  private void checkDepth() {
    if (currentDepth > maxDepth) {
      throw unsupportedOperationException("Max depth of '{}' is exceeded for filter path '{}'", maxDepth,
          fieldPath.stream()
              .map(ObjectField::getName)
              .collect(Collectors.joining(".")));
    }
  }

  private Stream<String> getArgumentKeys() {
    return argument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(argument.get(filterName)));
  }

}
