package org.dotwebstack.framework.core.config.validators;

import static org.springframework.util.StringUtils.uncapitalize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.springframework.stereotype.Component;

@Component
public class FilterValidator implements DotWebStackConfigurationValidator {

  @Override
  public void validate(DotWebStackConfiguration dotWebStackConfiguration) {
    List<String> validSortFields = ValidSortAndFilterFields.get(dotWebStackConfiguration);

    Map<String, Map<String, FilterConfiguration>> filtersPerObjectTypeName =
        getFiltersPerObjectTypeName(dotWebStackConfiguration);

    filtersPerObjectTypeName.forEach((objectTypeName, filters) -> filters.entrySet()
        .forEach(filterEntry -> validateFilterField(validSortFields, objectTypeName, filterEntry)));
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

  private void validateFilterField(List<String> validSortFields, String objectTypeName,
      Map.Entry<String, FilterConfiguration> filterEntry) {
    String filterFieldName = getFilterFieldName(objectTypeName, filterEntry);

    if (!validSortFields.contains(filterFieldName)) {
      throw new InvalidConfigurationException(
          String.format("Filter field '%s' in object type '%s' can't be resolved to a single scalar type.",
              filterFieldName, objectTypeName));
    }
  }

  private String getFilterFieldName(String objectTypeName, Map.Entry<String, FilterConfiguration> filterEntry) {
    String fieldName;

    if (filterEntry.getValue()
        .getField() != null) {
      fieldName = filterEntry.getValue()
          .getField();
    } else {
      fieldName = filterEntry.getKey();
    }

    return uncapitalize(objectTypeName).concat(".")
        .concat(fieldName);
  }
}
