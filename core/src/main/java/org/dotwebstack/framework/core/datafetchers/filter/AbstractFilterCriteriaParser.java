package org.dotwebstack.framework.core.datafetchers.filter;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import graphql.schema.GraphQLInputObjectField;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

public abstract class AbstractFilterCriteriaParser implements FilterCriteriaParser {

  Optional<Object> getChildData(GraphQLInputObjectField inputObjectField, FilterConfiguration filterConfiguration,
      Map<String, Object> data) {
    if (data.containsKey(inputObjectField.getName())) {
      return ofNullable(data.get(inputObjectField.getName()));
    }

    if (filterConfiguration.hasDefaultValue()) {
      return ofNullable(filterConfiguration.getDefaultValue());
    }

    return empty();
  }

  Optional<Filter> getFilter(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data) {
    var filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    return getChildData(inputObjectField, filterConfiguration, data).map(childData -> Filter.builder()
        .inputObjectField(inputObjectField)
        .data(childData)
        .build());
  }

  FieldConfiguration getFieldConfiguration(TypeConfiguration<?> typeConfiguration,
      GraphQLInputObjectField inputObjectField) {
    var filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    var fieldName = Optional.ofNullable(filterConfiguration.getField())
        .orElse(inputObjectField.getName());

    return typeConfiguration.getField(fieldName)
        .orElseThrow(IllegalStateException::new);
  }

  String getFieldPath(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField) {
    var result = "";
    var filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());
    if (StringUtils.contains(filterConfiguration.getField(), ".")) {
      result = filterConfiguration.getField();
    }
    return result;
  }

  @Data
  @Builder
  static class Filter {
    private GraphQLInputObjectField inputObjectField;

    private Object data;
  }
}
