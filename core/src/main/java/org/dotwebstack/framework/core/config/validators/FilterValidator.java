package org.dotwebstack.framework.core.config.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(GraphQlNativeEnabled.class)
@Component
public class FilterValidator implements SchemaValidator {

  @Override
  public void validate(Schema schema) {
    Map<String, Map<String, FilterConfiguration>> filtersPerObjectTypeName = getFiltersPerObjectTypeName(schema);

    filtersPerObjectTypeName.forEach((objectTypeName, filters) -> filters.entrySet()
        .forEach(filterEntry -> validateFilterField(schema.getObjectType(objectTypeName)
            .orElseThrow(), filterEntry)));
  }

  private Map<String, Map<String, FilterConfiguration>> getFiltersPerObjectTypeName(Schema schema) {
    return schema.getObjectTypes()
        .entrySet()
        .stream()
        .filter(this::isFilterListNotEmpty)
        .collect(Collectors.toMap(Map.Entry::getKey, this::getFilterConfiguration));
  }

  private boolean isFilterListNotEmpty(Map.Entry<String, ? extends ObjectType<?>> entry) {
    return !entry.getValue()
        .getFilters()
        .isEmpty();
  }

  private Map<String, FilterConfiguration> getFilterConfiguration(Map.Entry<String, ? extends ObjectType<?>> entry) {
    return entry.getValue()
        .getFilters();
  }

  private void validateFilterField(ObjectType<?> objectType, Map.Entry<String, FilterConfiguration> filterEntry) {
    String filterFieldName = getFilterFieldName(filterEntry);

    if (objectType.getField(filterFieldName)
        .isEmpty()) {
      throw invalidConfigurationException(
          "Filter field '{}' in object type '{}' can't be resolved to a single scalar type.", filterFieldName,
          objectType.getName());
    }
  }

  private String getFilterFieldName(Map.Entry<String, FilterConfiguration> filterEntry) {

    return (filterEntry.getValue()
        .getField() != null) ? filterEntry.getValue()
            .getField() : filterEntry.getKey();
  }
}
