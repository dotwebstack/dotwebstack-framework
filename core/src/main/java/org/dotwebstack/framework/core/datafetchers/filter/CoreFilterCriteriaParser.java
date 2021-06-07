package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
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
  protected FilterCriteria createFilterCriteria(FieldPath fieldPath, FieldConfiguration fieldConfiguration,
      FilterItem filterItem) {
    switch (filterItem.getOperator()) {
      case FilterConstants.EQ_FIELD:
        return createEqualsFilterCriteria(fieldPath, filterItem);
      case FilterConstants.LT_FIELD:
        return createLowerThenFilterCriteria(fieldPath, filterItem);
      case FilterConstants.LTE_FIELD:
        return createLowerThenEqualsFilterCriteria(fieldPath, filterItem);
      case FilterConstants.GT_FIELD:
        return createGreaterThenFilterCriteria(fieldPath, filterItem);
      case FilterConstants.GTE_FIELD:
        return createGreaterThenEqualsFilterCriteria(fieldPath, filterItem);
      case FilterConstants.IN_FIELD:
        return createInFilterCriteria(fieldPath, filterItem);
      default:
        return super.createFilterCriteria(fieldPath, fieldConfiguration, filterItem);
    }
  }

  private FilterCriteria createLowerThenFilterCriteria(FieldPath fieldPath, FilterItem filterItem) {
    return LowerThenFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createLowerThenEqualsFilterCriteria(FieldPath fieldPath, FilterItem filterItem) {
    return LowerThenEqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createGreaterThenFilterCriteria(FieldPath fieldPath, FilterItem filterItem) {
    return GreaterThenFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createGreaterThenEqualsFilterCriteria(FieldPath fieldPath, FilterItem filterItem) {
    return GreaterThenEqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createEqualsFilterCriteria(FieldPath fieldPath, FilterItem filterItem) {
    return EqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(filterItem.getValue())
        .build();
  }

  private FilterCriteria createInFilterCriteria(FieldPath fieldPath, FilterItem filterItem) {
    if (!(filterItem.getValue() instanceof List)) {
      throw illegalArgumentException("Filter item value not of type List!");
    }

    return InFilterCriteria.builder()
        .fieldPath(fieldPath)
        .values((List<?>) filterItem.getValue())
        .build();
  }
}
