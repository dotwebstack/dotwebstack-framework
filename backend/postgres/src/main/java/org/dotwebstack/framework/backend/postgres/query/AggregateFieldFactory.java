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

import graphql.schema.SelectedField;
import java.math.BigDecimal;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class AggregateFieldFactory {

  public Field<?> create(SelectedField selectedField, String fromTable, String columnName) {
    Field<?> result;
    String aggregateFunction = selectedField.getName();

    // TODO enum for AggregateFunctions?
    switch (aggregateFunction) {
      case COUNT_FIELD:
        boolean distinct = (Boolean) selectedField.getArguments()
            .get(DISTINCT_ARGUMENT);
        if (distinct) {
          result = DSL.count(DSL.field(DSL.name(fromTable, columnName)));
        } else {
          result = DSL.countDistinct(DSL.field(DSL.name(fromTable, columnName)));
        }
        break;
      case INT_SUM_FIELD:
        result = DSL.sum(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class))
            .cast(Integer.class);
        break;
      case INT_MIN_FIELD:
        result = DSL.min(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class))
            .cast(Integer.class);
        break;
      case INT_MAX_FIELD:
        result = DSL.max(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class))
            .cast(Integer.class);
        break;
      case INT_AVG_FIELD:
        result = DSL.avg(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class))
            .cast(Integer.class);
        break;
      case FLOAT_SUM_FIELD:
        result = DSL.sum(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
      case FLOAT_MIN_FIELD:
        result = DSL.min(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
      case FLOAT_MAX_FIELD:
        result = DSL.max(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
      case FLOAT_AVG_FIELD:
        result = DSL.avg(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
      default:
        throw new IllegalArgumentException(String.format("Aggregate function %s is not supported", aggregateFunction));
    }

    return result;
  }
}
