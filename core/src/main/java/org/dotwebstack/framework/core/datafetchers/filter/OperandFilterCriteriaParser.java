package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.filter.AndFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.InFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;
import org.springframework.stereotype.Component;

@Component
public class OperandFilterCriteriaParser extends AbstractFilterCriteriaParser {

  private static final List<String> SUPPORTED_OBJECT_TYPES = List.of(FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE,
      FilterConstants.DATE_FILTER_INPUT_OBJECT_TYPE, FilterConstants.INT_FILTER_INPUT_OBJECT_TYPE,
      FilterConstants.FLOAT_FILTER_INPUT_OBJECT_TYPE, FilterConstants.DATE_TIME_FILTER_INPUT_OBJECT_TYPE);

  @Override
  public boolean supports(GraphQLInputObjectField inputObjectField) {
    return SUPPORTED_OBJECT_TYPES.contains(getTypeName(inputObjectField.getType()));
  }

  @Override
  public List<FilterCriteria> parse(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data) {
    return getFilter(typeConfiguration, inputObjectField, data).stream()
        .flatMap(filter -> getFilterItems(filter).stream())
        .map(filterItem -> createFilterCriteria(getFieldConfiguration(typeConfiguration, inputObjectField), filterItem))
        .collect(Collectors.toList());
  }

  private FilterCriteria createFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    switch (filterItem.getOperator()) {
      case EQ:
        return createEqualsFilterCriteria(fieldConfiguration, filterItem);
      case LT:
        return createLowerThenFilterCriteria(fieldConfiguration, filterItem);
      case LTE:
        return createLowerThenEqualsFilterCriteria(fieldConfiguration, filterItem);
      case GT:
        return createGreaterThenFilterCriteria(fieldConfiguration, filterItem);
      case GTE:
        return createGreaterThenEqualsFilterCriteria(fieldConfiguration, filterItem);
      case IN:
        return createInFilterCriteria(fieldConfiguration, filterItem);
      case NOT:
        return createNotFilterCriteria(fieldConfiguration, filterItem);
      default:
        throw unsupportedOperationException("Filter operator '{}' is not supported!", filterItem.getOperator());
    }
  }

  private FilterCriteria createLowerThenFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    return LowerThenFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createLowerThenEqualsFilterCriteria(FieldConfiguration fieldConfiguration,
      FilterItem filterItem) {
    return LowerThenEqualsFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createGreaterThenFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    return GreaterThenFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createGreaterThenEqualsFilterCriteria(FieldConfiguration fieldConfiguration,
      FilterItem filterItem) {
    return GreaterThenEqualsFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createNotFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    FilterCriteria innerCriteria;
    if (filterItem.getChildren()
        .size() > 1) {
      innerCriteria = AndFilterCriteria.builder()
          .filterCriterias(filterItem.getChildren()
              .stream()
              .map(innerFilterItem -> createFilterCriteria(fieldConfiguration, innerFilterItem))
              .collect(Collectors.toList()))
          .build();
    } else {
      innerCriteria = createFilterCriteria(fieldConfiguration, filterItem.getChildren()
          .get(0));
    }

    return NotFilterCriteria.builder()
        .filterCriteria(innerCriteria)
        .build();
  }

  private FilterCriteria createEqualsFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    return EqualsFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createInFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    if (!(filterItem.getValue() instanceof List)) {
      throw illegalArgumentException("Filter item value not of type List!");
    }

    return InFilterCriteria.builder()
        .field(fieldConfiguration)
        .values((List<?>) filterItem.getValue())
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
    FilterOperator filterOperator = FilterOperator.valueOf(inputObjectField.getName()
        .toUpperCase());

    return FilterItem.builder()
        .operator(filterOperator)
        .value(data.get(inputObjectField.getName()))
        .children(getInputObjectTypes(inputObjectField).flatMap(
            inputObjectType -> getFilterItems(inputObjectType, getNestedMap(data, inputObjectField.getName())).stream())
            .collect(Collectors.toList()))
        .build();
  }

  private Stream<GraphQLInputObjectType> getInputObjectTypes(GraphQLInputObjectField inputObjectField) {
    return inputObjectField.getChildren()
        .stream()
        .filter(schemaElement -> schemaElement instanceof GraphQLInputObjectType)
        .map(GraphQLInputObjectType.class::cast);
  }

  private Stream<GraphQLInputObjectField> getInputObjectFields(GraphQLInputObjectType inputObjectType) {
    return inputObjectType.getChildren()
        .stream()
        .filter(schemaElement -> schemaElement instanceof GraphQLInputObjectField)
        .map(GraphQLInputObjectField.class::cast);
  }

  @Data
  @Builder
  private static class FilterItem {
    private FieldConfiguration fieldConfiguration;

    private FilterOperator operator;

    private Object value;

    private List<FilterItem> children;
  }

  private enum FilterOperator {
    EQ, IN, NOT, LT, LTE, GT, GTE
  }
}
