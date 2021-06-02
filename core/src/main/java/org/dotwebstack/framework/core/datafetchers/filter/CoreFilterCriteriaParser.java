package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.InFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenFilterCriteria;
import org.springframework.stereotype.Component;

@Component
public class CoreFilterCriteriaParser extends OperatorFilterCriteriaParser {

  private static final List<String> SUPPORTED_OBJECT_TYPES = List.of(FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE,
      FilterConstants.DATE_FILTER_INPUT_OBJECT_TYPE, FilterConstants.INT_FILTER_INPUT_OBJECT_TYPE,
      FilterConstants.FLOAT_FILTER_INPUT_OBJECT_TYPE, FilterConstants.DATE_TIME_FILTER_INPUT_OBJECT_TYPE);

  @Override
  public boolean supports(GraphQLInputObjectField inputObjectField) {
    return SUPPORTED_OBJECT_TYPES.contains(getTypeName(inputObjectField.getType()));
  }

  @Override
  protected FilterCriteria createFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    switch (filterItem.getOperator()) {
      case FilterConstants.EQ_FIELD:
        return createEqualsFilterCriteria(fieldConfiguration, filterItem);
      case FilterConstants.LT_FIELD:
        return createLowerThenFilterCriteria(fieldConfiguration, filterItem);
      case FilterConstants.LTE_FIELD:
        return createLowerThenEqualsFilterCriteria(fieldConfiguration, filterItem);
      case FilterConstants.GT_FIELD:
        return createGreaterThenFilterCriteria(fieldConfiguration, filterItem);
      case FilterConstants.GTE_FIELD:
        return createGreaterThenEqualsFilterCriteria(fieldConfiguration, filterItem);
      case FilterConstants.IN_FIELD:
        return createInFilterCriteria(fieldConfiguration, filterItem);
      default:
        return super.createFilterCriteria(fieldConfiguration, filterItem);
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
}
