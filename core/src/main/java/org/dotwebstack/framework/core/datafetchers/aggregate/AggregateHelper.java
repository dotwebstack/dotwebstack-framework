package org.dotwebstack.framework.core.datafetchers.aggregate;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.DISTINCT_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.SEPARATOR_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.UNSUPPORTED_TYPE_ERROR_TEXT;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;

import graphql.schema.SelectedField;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.ScalarType;

public class AggregateHelper {

  private static final String DEFAULT_SEPARATOR = ",";

  private AggregateHelper() {}

  public static boolean isAggregateField(SelectedField selectedField) {
    if (selectedField == null) {
      return false;
    }

    return TypeHelper.getTypeName(selectedField.getType())
        .map(AggregateConstants.AGGREGATE_TYPE::equals)
        .orElseThrow(() -> illegalArgumentException(UNSUPPORTED_TYPE_ERROR_TEXT, selectedField.getType()
            .getClass()));
  }

  public static boolean isAggregate(ObjectField objectField) {
    return !StringUtils.isEmpty(objectField.getAggregationOf());
  }

  public static ScalarType getAggregateScalarType(SelectedField selectedField) {
    String aggregateFunction = selectedField.getName();
    return switch (aggregateFunction) {
      case INT_MIN_FIELD, INT_MAX_FIELD, INT_AVG_FIELD, INT_SUM_FIELD, COUNT_FIELD -> ScalarType.INT;
      case STRING_JOIN_FIELD -> ScalarType.STRING;
      case FLOAT_MIN_FIELD, FLOAT_SUM_FIELD, FLOAT_MAX_FIELD, FLOAT_AVG_FIELD -> ScalarType.FLOAT;
      default -> throw requestValidationException("Aggregate function {} is not supported", aggregateFunction);
    };
  }

  public static AggregateFunctionType getAggregateFunctionType(SelectedField selectedField) {
    String aggregateFunction = selectedField.getName();
    return switch (aggregateFunction) {
      case COUNT_FIELD -> AggregateFunctionType.COUNT;
      case STRING_JOIN_FIELD -> AggregateFunctionType.JOIN;
      case FLOAT_SUM_FIELD, INT_SUM_FIELD -> AggregateFunctionType.SUM;
      case FLOAT_MIN_FIELD, INT_MIN_FIELD -> AggregateFunctionType.MIN;
      case FLOAT_MAX_FIELD, INT_MAX_FIELD -> AggregateFunctionType.MAX;
      case INT_AVG_FIELD, FLOAT_AVG_FIELD -> AggregateFunctionType.AVG;
      default -> throw requestValidationException("Aggregate function {} is not supported", aggregateFunction);
    };
  }

  public static boolean isDistinct(SelectedField selectedField) {
    return Optional.ofNullable((Boolean) selectedField.getArguments()
        .get(DISTINCT_ARGUMENT))
        .orElse(Boolean.FALSE);
  }

  public static String getSeparator(SelectedField selectedField) {
    return Optional.ofNullable((String) selectedField.getArguments()
        .get(SEPARATOR_ARGUMENT))
        .orElse(DEFAULT_SEPARATOR);
  }
}
