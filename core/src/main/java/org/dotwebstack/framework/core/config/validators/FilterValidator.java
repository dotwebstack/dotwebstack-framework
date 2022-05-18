package org.dotwebstack.framework.core.config.validators;


import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.Scalars;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class FilterValidator implements SchemaValidator {

  @Override
  public void validate(Schema schema) {
    Map<String, Map<String, FilterConfiguration>> filtersPerObjectTypeName = getFiltersPerObjectTypeName(schema);

    filtersPerObjectTypeName.forEach((objectTypeName, filters) -> filters
        .forEach((key, value) -> validateFilterField(schema, objectTypeName, value)));
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

  private void validateFilterField(Schema schema, String objectTypeName, FilterConfiguration filter) {
    var filterFieldName = getFilterField(filter);

    ObjectField objectField = schema.getObjectType(objectTypeName)
        .filter(objectType -> objectType.getFields()
            .containsKey(filterFieldName))
        .map(objectType -> objectType.getField(filterFieldName))
        .orElseThrow(() -> invalidConfigurationException("Filter field '{}' not found in object type '{}'.",
            filterFieldName, objectTypeName));

    validateCaseSensitive(filter, objectField);
    validatePartialType(filter, objectField);
    validateDependsOn(filter, objectField);
  }

  private String getFilterField(FilterConfiguration filter) {
    return Optional.ofNullable(filter.getField())
        .orElse(filter.getName());
  }

  private void validateCaseSensitive(FilterConfiguration filter, ObjectField objectField) {
    if (filter.isCaseSensitive()) {
      return;
    }
    if (objectField.isEnumeration()) {
      throw invalidConfigurationException(
          "Filter '{}' with property 'caseSensitive' is 'false' not valid for enumerations.", filter.getName());
    }
    if (!Objects.equals(objectField.getType(), Scalars.GraphQLString.getName())) {
      throw invalidConfigurationException(
          "Filter '{}' with property 'caseSensitive' is 'false' not valid for type '{}'.", filter.getName(),
          objectField.getType());
    }
  }

  private void validatePartialType(FilterConfiguration filter, ObjectField objectField) {
    if (FilterType.PARTIAL.equals(filter.getType())
        && !Objects.equals(objectField.getType(), Scalars.GraphQLString.getName())) {
      throw invalidConfigurationException("Filter '{}' of type 'Partial' doesn´t refer to a 'String' field type.",
          filter.getName());
    }
  }

  private void validateDependsOn(FilterConfiguration filter, ObjectField objectField) {
    if (filter.getDependsOn() == null) {
      return;
    }
    if (filter.getDependsOn()
        .equals(filter.getName())) {
      throw invalidConfigurationException("Filter '{}' can't refer to oneself.", filter.getName());
    }
    FilterConfiguration dependsOn = Optional.ofNullable(objectField.getObjectType()
        .getFilters()
        .get(filter.getDependsOn()))
        .orElseThrow(() -> invalidConfigurationException("Filter '{}' depends on non existing filter '{}'.",
            filter.getName(), filter.getDependsOn()));
    validateDependsOn(dependsOn, objectField);
  }
}
