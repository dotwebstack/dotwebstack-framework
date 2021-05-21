package org.dotwebstack.framework.core.datafetchers.filter;

import graphql.schema.GraphQLInputObjectField;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

public abstract class AbstractFilterCriteriaParser implements FilterCriteriaParser {

  Optional<Object> getChildData(GraphQLInputObjectField inputObjectField, FilterConfiguration filterConfiguration,
      Map<String, Object> data) {
    if (data.containsKey(inputObjectField.getName())) {
      return Optional.ofNullable(data.get(inputObjectField.getName()));
    }

    if (filterConfiguration.hasDefaultValue()) {
      return Optional.ofNullable(filterConfiguration.getDefaultValue());
    }

    return Optional.empty();
  }

  Optional<Filter> getFilter(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data) {
    FilterConfiguration filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    return getChildData(inputObjectField, filterConfiguration, data).map(childData -> Filter.builder()
        .inputObjectField(inputObjectField)
        .data(childData)
        .build());
  }

  FieldConfiguration getFieldConfiguration(TypeConfiguration<?> typeConfiguration,
      GraphQLInputObjectField inputObjectField) {
    FilterConfiguration filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    return typeConfiguration.getFields()
        .get(filterConfiguration.getField());
  }
}
