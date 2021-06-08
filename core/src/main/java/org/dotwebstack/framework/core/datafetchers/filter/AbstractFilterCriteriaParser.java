package org.dotwebstack.framework.core.datafetchers.filter;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

public abstract class AbstractFilterCriteriaParser implements FilterCriteriaParser {

  protected Optional<Object> getChildData(GraphQLInputObjectField inputObjectField,
      FilterConfiguration filterConfiguration, Map<String, Object> data) {
    if (data.containsKey(inputObjectField.getName())) {
      return ofNullable(data.get(inputObjectField.getName()));
    }

    if (filterConfiguration.hasDefaultValue()) {
      return ofNullable(filterConfiguration.getDefaultValue());
    }

    return empty();
  }

  protected Optional<Filter> getFilter(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data) {
    var filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    return getChildData(inputObjectField, filterConfiguration, data).map(childData -> Filter.builder()
        .inputObjectField(inputObjectField)
        .data(childData)
        .build());
  }

  protected FieldConfiguration getFieldConfiguration(TypeConfiguration<?> typeConfiguration,
      GraphQLInputObjectField inputObjectField) {
    var filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    var fieldName = Optional.ofNullable(filterConfiguration.getField())
        .orElse(inputObjectField.getName());

    return typeConfiguration.getField(fieldName)
        .orElseThrow(IllegalStateException::new);
  }

  protected String getFieldPath(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField) {
    var filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    if (StringUtils.isNotBlank(filterConfiguration.getField())) {
      return filterConfiguration.getField();
    }

    return inputObjectField.getName();
  }

  protected Stream<GraphQLInputObjectType> getInputObjectTypes(GraphQLInputObjectField inputObjectField) {
    return inputObjectField.getChildren()
        .stream()
        .filter(GraphQLInputObjectType.class::isInstance)
        .map(GraphQLInputObjectType.class::cast);
  }

  protected Stream<GraphQLInputObjectField> getInputObjectFields(GraphQLInputObjectType inputObjectType) {
    return inputObjectType.getChildren()
        .stream()
        .filter(GraphQLInputObjectField.class::isInstance)
        .map(GraphQLInputObjectField.class::cast);
  }

  @Data
  @Builder
  protected static class Filter {
    private GraphQLInputObjectField inputObjectField;

    private Object data;
  }
}
