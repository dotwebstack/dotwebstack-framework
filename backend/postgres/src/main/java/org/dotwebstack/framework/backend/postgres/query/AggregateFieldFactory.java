package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.SelectedField;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.DISTINCT_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MIN_FIELD;
import static  org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_SUM_FIELD;
import static  org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;

import org.jooq.AggregateFunction;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class AggregateFieldFactory {
//  count(field: String!, distinct: Boolean = false): Int!
//
//  intSum(field: String!): Int
//  intMin(field: String!): Int
//  intMax(field: String!): Int
//  intAvg(field: String!): Int
//
//  floatSum(field: String!): Float
//  floatMin(field: String!): Float
//  floatMax(field: String!): Float
//  floatAvg(field: String!): Float

  public static Field<?> create(SelectedField selectedField, String fromTable, String columnName){
    Field<?> result = null;
    String aggregateFunction = selectedField.getName();

    // TODO enum for AggregateFunctions?
    switch(aggregateFunction) {
      case COUNT_FIELD:
        boolean distinct = (Boolean)selectedField.getArguments().get(DISTINCT_ARGUMENT);
        if(distinct) {
          result = DSL.count(DSL.field(DSL.name(fromTable, columnName)));
        } else {
          result = DSL.countDistinct(DSL.field(DSL.name(fromTable, columnName)));
        }
        break;
      case INT_SUM_FIELD:
        result =  DSL.sum(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class)).cast(Integer.class);
        break;
      case INT_MIN_FIELD:
        result =  DSL.min(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class)).cast(Integer.class);
        break;
      case INT_MAX_FIELD:
//        result =  DSL.coalesce(DSL.max(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class)), BigDecimal.ZERO);
        result =  DSL.max(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class)).cast(Integer.class);
        break;
      case INT_AVG_FIELD:
//        result =  DSL.coalesce(DSL.avg(DSL.field(DSL.name(fromTable, columnName), Integer.class)), Integer.valueOf(0));
//        result =  DSL.avg(DSL.field(DSL.name(fromTable, columnName), Integer.class)).cast(Integer.class);
        result =  DSL.avg(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class)).cast(Integer.class);
        break;
      case FLOAT_SUM_FIELD:
        result =  DSL.sum(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
      case FLOAT_MIN_FIELD:
        result =  DSL.min(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
      case FLOAT_MAX_FIELD:
        result =  DSL.max(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
      case FLOAT_AVG_FIELD:
        result =  DSL.avg(DSL.field(DSL.name(fromTable, columnName), BigDecimal.class));
        break;
    }

    return Optional.ofNullable(result).orElseThrow(() -> new IllegalArgumentException(String.format("Aggregate function %s is not supported", aggregateFunction)));
  }
}
