package org.dotwebstack.framework.core.config.validators;

import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(DotWebStackConfiguration.class)
public class FilterValidator implements DotWebStackConfigurationValidator {

  @Override
  public void validate(DotWebStackConfiguration dotWebStackConfiguration) {
    Map<String, Map<String, FilterConfiguration>> filtersPerObjectTypeName =
        getFiltersPerObjectTypeName(dotWebStackConfiguration);

    filtersPerObjectTypeName.forEach((objectType, filters) -> filters.entrySet()
        .forEach(filterEntry -> validateFilterField(dotWebStackConfiguration.getTypeConfiguration(objectType),
            filterEntry)));
  }

  private Map<String, Map<String, FilterConfiguration>> getFiltersPerObjectTypeName(
      DotWebStackConfiguration dotWebStackConfiguration) {
    return dotWebStackConfiguration.getObjectTypes()
        .entrySet()
        .stream()
        .filter(this::isFilterListNotEmpty)
        .collect(Collectors.toMap(Map.Entry::getKey, this::getFilterConfiguration));
  }

  private boolean isFilterListNotEmpty(Map.Entry<String, AbstractTypeConfiguration<?>> entry) {
    return !entry.getValue()
        .getFilters()
        .isEmpty();
  }

  private Map<String, FilterConfiguration> getFilterConfiguration(
      Map.Entry<String, AbstractTypeConfiguration<?>> entry) {
    return entry.getValue()
        .getFilters();
  }

  private void validateFilterField(AbstractTypeConfiguration<?> objectType,
      Map.Entry<String, FilterConfiguration> filterEntry) {
    String filterFieldName = getFilterFieldName(filterEntry);

    if (!objectType.getField(filterFieldName)
        .isPresent()) {
      throw new InvalidConfigurationException(
          String.format("Filter field '%s' in object type '%s' can't be resolved to a single scalar type.",
              filterFieldName, objectType.getName()));
    }
  }

  private String getFilterFieldName(Map.Entry<String, FilterConfiguration> filterEntry) {

    return (filterEntry.getValue()
        .getField() != null) ? filterEntry.getValue()
            .getField() : filterEntry.getKey();
  }
}
