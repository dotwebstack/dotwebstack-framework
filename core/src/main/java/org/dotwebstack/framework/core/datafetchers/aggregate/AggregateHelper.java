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
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.ScalarType;

public class AggregateHelper {

  private static final String DEFAULT_SEPARATOR = ",";

  private AggregateHelper() {}

  public static boolean isAggregateField(SelectedField selectedField) {
    if (selectedField == null) {
      return false;
    }

    GraphQLObjectType objectType = selectedField.getObjectType();

    return AggregateConstants.AGGREGATE_TYPE.equals(TypeHelper.getTypeName(objectType));
  }

  public static boolean isAggregate(FieldConfiguration fieldConfiguration) {
    return !StringUtils.isEmpty(fieldConfiguration.getAggregationOf());
  }

  public static ScalarType getAggregateScalarType(SelectedField selectedField) {
    String aggregateFunction = selectedField.getName();
    switch (aggregateFunction) {
      case INT_MIN_FIELD:
      case INT_MAX_FIELD:
      case INT_AVG_FIELD:
      case INT_SUM_FIELD:
      case COUNT_FIELD:
        return ScalarType.INT;
      case STRING_JOIN_FIELD:
        return ScalarType.STRING;
      case FLOAT_MIN_FIELD:
      case FLOAT_SUM_FIELD:
      case FLOAT_MAX_FIELD:
      case FLOAT_AVG_FIELD:
        return ScalarType.FLOAT;
      default:
        throw illegalArgumentException("Aggregate function {} is not supported", aggregateFunction);
    }
  }

  public static AggregateFunctionType getAggregateFunctionType(SelectedField selectedField) {
    String aggregateFunction = selectedField.getName();
    switch (aggregateFunction) {
      case COUNT_FIELD:
        return AggregateFunctionType.COUNT;
      case STRING_JOIN_FIELD:
        return AggregateFunctionType.JOIN;
      case FLOAT_SUM_FIELD:
      case INT_SUM_FIELD:
        return AggregateFunctionType.SUM;
      case FLOAT_MIN_FIELD:
      case INT_MIN_FIELD:
        return AggregateFunctionType.MIN;
      case FLOAT_MAX_FIELD:
      case INT_MAX_FIELD:
        return AggregateFunctionType.MAX;
      case INT_AVG_FIELD:
      case FLOAT_AVG_FIELD:
        return AggregateFunctionType.AVG;
      default:
        throw illegalArgumentException("Aggregate function {} is not supported", aggregateFunction);
    }
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
