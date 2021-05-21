package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
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
public class FilterCriteriaFactory {

  public List<FilterCriteria> getFilterCriterias(TypeConfiguration<?> typeConfiguration,
      GraphQLInputObjectType inputObjectType, Map<String, Object> data) {

    List<Filter> filters = getFilters(typeConfiguration, inputObjectType, data);

    return filters.stream()
        .flatMap(filter -> {
          List<FilterItem> filterItems = getFilterItems(filter);

          return filterItems.stream()
              .map(filterItem -> createFilterCriteria(filter.getFieldConfiguration(), filterItem));

        })
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

  private List<Filter> getFilters(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectType inputObjectType,
      Map<String, Object> data) {

    if (data == null || data.isEmpty()) {
      return List.of();
    }

    return getInputObjectFields(inputObjectType)
        .map(inputObjectField -> getFilterItem(typeConfiguration, inputObjectField, data))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private List<FilterItem> getFilterItems(Filter filter) {
    return getInputObjectTypes(filter.getInputObjectField())
        .flatMap(inputObjectType -> getFilterItems(inputObjectType, filter.getData()).stream())
        .collect(Collectors.toList());
  }

  private List<FilterItem> getFilterItems(GraphQLInputObjectType inputObjectType, Map<String, Object> data) {
    return getInputObjectFields(inputObjectType)
        .filter(inputObjectField -> Objects.nonNull(data.get(inputObjectField.getName())))
        .map(inputObjectField -> createFilterItem(data, inputObjectField).build())
        .collect(Collectors.toList());
  }

  private FilterItem.FilterItemBuilder createFilterItem(Map<String, Object> data,
      GraphQLInputObjectField inputObjectField) {

    FilterOperator filterOperator = FilterOperator.valueOf(inputObjectField.getName()
        .toUpperCase());

    return FilterItem.builder()
        .operator(filterOperator)
        .value(data.get(inputObjectField.getName()))
        .children(getInputObjectTypes(inputObjectField).flatMap(
            inputObjectType -> getFilterItems(inputObjectType, getNestedMap(data, inputObjectField.getName())).stream())
            .collect(Collectors.toList()));
  }

  private Optional<Filter> getFilterItem(TypeConfiguration<?> typeConfiguration,
      GraphQLInputObjectField inputObjectField, Map<String, Object> data) {
    Map<String, Object> childData = getNestedMap(data, inputObjectField.getName());

    if (childData == null || childData.isEmpty()) {
      return Optional.empty();
    }

    FilterConfiguration filterConfiguration = typeConfiguration.getFilters()
        .get(inputObjectField.getName());

    AbstractFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
        .get(filterConfiguration.getField());

    return Optional.of(Filter.builder()
        .inputObjectField(inputObjectField)
        .filterConfiguration(filterConfiguration)
        .fieldConfiguration(fieldConfiguration)
        .data(childData)
        .build());
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
}
