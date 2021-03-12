package org.dotwebstack.framework.backend.postgres.query;

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
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.SelectedField;
import java.math.BigDecimal;
import java.util.Optional;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class AggregateFieldFactory {

  public Field<?> create(SelectedField selectedField, String fromTable, String columnName) {
    Field<?> result;
    String aggregateFunction = selectedField.getName();

    switch (aggregateFunction) {
      case COUNT_FIELD:
        if (isCountDistinct(selectedField)) {
          result = DSL.countDistinct(DSL.field(DSL.name(fromTable, columnName)));
        } else {
          result = DSL.count(DSL.field(DSL.name(fromTable, columnName)));
        }
        break;
      case INT_SUM_FIELD:
        result = DSL.sum(bigDecimalField(fromTable, columnName))
            .cast(Integer.class);
        break;
      case INT_MIN_FIELD:
        result = DSL.min(bigDecimalField(fromTable, columnName))
            .cast(Integer.class);
        break;
      case INT_MAX_FIELD:
        result = DSL.max(bigDecimalField(fromTable, columnName))
            .cast(Integer.class);
        break;
      case INT_AVG_FIELD:
        result = DSL.avg(bigDecimalField(fromTable, columnName))
            .cast(Integer.class);
        break;
      case FLOAT_SUM_FIELD:
        result = DSL.sum(bigDecimalField(fromTable, columnName));
        break;
      case FLOAT_MIN_FIELD:
        result = DSL.min(bigDecimalField(fromTable, columnName));
        break;
      case FLOAT_MAX_FIELD:
        result = DSL.max(bigDecimalField(fromTable, columnName));
        break;
      case FLOAT_AVG_FIELD:
        result = DSL.avg(bigDecimalField(fromTable, columnName));
        break;
      default:
        throw illegalArgumentException("Aggregate function {} is not supported", aggregateFunction);
    }

    return result;
  }

  private boolean isCountDistinct(SelectedField selectedField){
    return Optional.ofNullable((Boolean)selectedField.getArguments()
        .get(DISTINCT_ARGUMENT)).orElse(Boolean.FALSE);
  }

  private Field<BigDecimal> bigDecimalField(String fromTable, String columnName) {
    return DSL.field(DSL.name(fromTable, columnName), BigDecimal.class);
  }
}
