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

    filtersPerObjectTypeName.forEach((objectTypeName, filters) -> filters.entrySet()
        .forEach(filterEntry -> validateFilterField(schema, objectTypeName, filterEntry)));
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

  private void validateFilterField(Schema schema, String objectTypeName,
      Map.Entry<String, FilterConfiguration> filterEntry) {
    String filterFieldName = getFilterField(filterEntry);

    ObjectField objectField = schema.getObjectType(objectTypeName)
        .filter(objectType -> objectType.getFields()
            .containsKey(filterFieldName))
        .map(objectType -> objectType.getField(filterFieldName))
        .orElseThrow(() -> invalidConfigurationException("Filter field '{}' not found in object type '{}'.",
            filterFieldName, objectTypeName));

    var filterConfiguration = filterEntry.getValue();
    if (!filterConfiguration.isCaseSensitive()) {
      if (objectField.isEnumeration()) {
        throw invalidConfigurationException(
            "Filter '{}' with property 'caseSensitive' is 'false' not valid for enumerations.", filterEntry.getKey());
      }

      if (!Objects.equals(objectField.getType(), Scalars.GraphQLString.getName())) {
        throw invalidConfigurationException(
            "Filter '{}' with property 'caseSensitive' is 'false' not valid for type '{}'.", filterEntry.getKey(),
            objectField.getType());
      }
    }

    if (FilterType.PARTIAL.equals(filterConfiguration.getType())
        && !Objects.equals(objectField.getType(), Scalars.GraphQLString.getName())) {
      throw invalidConfigurationException(
          "Filter '{}' of type 'Term' in object type '{}' doesn´t refer to a 'String' field type.",
          filterEntry.getKey(), objectTypeName);
    }
  }

  private String getFilterField(Map.Entry<String, FilterConfiguration> filterEntry) {
    return Optional.ofNullable(filterEntry.getValue()
        .getField())
        .orElse(filterEntry.getKey());
  }
}
