package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.filter.AndFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FieldPathHelper;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;

public abstract class OperatorFilterCriteriaParser extends AbstractFilterCriteriaParser {

  @Override
  public List<FilterCriteria> parse(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data) {
    return getFilter(typeConfiguration, inputObjectField, data).stream()
        .flatMap(filter -> getFilterItems(filter).stream())
        .map(filterItem -> createFilterCriteria(
            FieldPathHelper.createFieldPath(typeConfiguration, getFieldPath(typeConfiguration, inputObjectField)),
            getFieldConfiguration(typeConfiguration, inputObjectField), filterItem))
        .collect(Collectors.toList());
  }

  protected FilterCriteria createFilterCriteria(FieldPath fieldPath, FieldConfiguration fieldConfiguration,
      FilterItem filterItem) {
    if (FilterConstants.NOT_FIELD.equals(filterItem.getOperator())) {
      return createNotFilterCriteria(fieldPath, fieldConfiguration, filterItem);
    }
    throw unsupportedOperationException("Filter operator '{}' is not supported!", filterItem.getOperator());
  }

  private FilterCriteria createNotFilterCriteria(FieldPath fieldPath, FieldConfiguration fieldConfiguration,
      FilterItem filterItem) {
    FilterCriteria innerCriteria;
    if (filterItem.getChildren()
        .size() > 1) {
      innerCriteria = AndFilterCriteria.builder()
          .filterCriterias(filterItem.getChildren()
              .stream()
              .map(innerFilterItem -> createFilterCriteria(fieldPath, fieldConfiguration, innerFilterItem))
              .collect(Collectors.toList()))
          .build();
    } else {
      innerCriteria = createFilterCriteria(fieldPath, fieldConfiguration, filterItem.getChildren()
          .get(0));
    }

    return NotFilterCriteria.builder()
        .filterCriteria(innerCriteria)
        .build();
  }

  @SuppressWarnings("unchecked")
  private List<FilterItem> getFilterItems(Filter filter) {
    return getInputObjectTypes(filter.getInputObjectField())
        .flatMap(inputObjectType -> getFilterItems(inputObjectType, (Map<String, Object>) filter.getData()).stream())
        .collect(Collectors.toList());
  }

  private List<FilterItem> getFilterItems(GraphQLInputObjectType inputObjectType, Map<String, Object> data) {
    return getInputObjectFields(inputObjectType)
        .filter(inputObjectField -> Objects.nonNull(data.get(inputObjectField.getName())))
        .map(inputObjectField -> createFilterItem(data, inputObjectField))
        .collect(Collectors.toList());
  }

  private FilterItem createFilterItem(Map<String, Object> data, GraphQLInputObjectField inputObjectField) {
    var filterOperator = inputObjectField.getName();

    return FilterItem.builder()
        .operator(filterOperator)
        .value(data.get(inputObjectField.getName()))
        .children(getInputObjectTypes(inputObjectField).flatMap(
            inputObjectType -> getFilterItems(inputObjectType, getNestedMap(data, inputObjectField.getName())).stream())
            .collect(Collectors.toList()))
        .build();
  }

  @Data
  @Builder
  protected static class FilterItem {
    private String operator;

    private Object value;

    private List<FilterItem> children;
  }
}
