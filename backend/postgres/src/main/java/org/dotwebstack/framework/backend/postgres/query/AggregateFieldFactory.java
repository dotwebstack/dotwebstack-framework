package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.math.BigDecimal;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class AggregateFieldFactory {

  public static Field<?> create(AggregateField aggregateFieldConfiguration, String fromTable, String columnName,
      String columnAlias) {
    Field<?> result;

    var aggregateFunction = aggregateFieldConfiguration.getFunctionType();
    switch (aggregateFunction) {
      case AVG:
        result = DSL.avg(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateFieldConfiguration.getType()));
        break;
      case COUNT:
        if (aggregateFieldConfiguration.isDistinct()) {
          result = DSL.countDistinct(DSL.field(DSL.name(fromTable, columnName)));
        } else {
          result = DSL.count(DSL.field(DSL.name(fromTable, columnName)));
        }
        break;
      case JOIN:
        result = createStringJoin(aggregateFieldConfiguration, fromTable, columnName, columnAlias);
        break;
      case MAX:
        result = DSL.max(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateFieldConfiguration.getType()));
        break;
      case MIN:
        result = DSL.min(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateFieldConfiguration.getType()));
        break;
      case SUM:
        result = DSL.sum(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateFieldConfiguration.getType()));
        break;
      default:
        throw illegalArgumentException("Aggregate function {} is not supported",
            aggregateFieldConfiguration.getFunctionType());
    }
    return result;
  }

  private static Field<?> createGroupConcat(AggregateField aggregateFieldConfiguration, String alias) {
    Field<?> result;
    String separator = aggregateFieldConfiguration.getSeparator();
    if (aggregateFieldConfiguration.isDistinct()) {
      result = DSL.groupConcatDistinct(DSL.field(DSL.name(alias)))
          .separator(separator);
    } else {
      result = DSL.groupConcat(DSL.field(DSL.name(alias)))
          .separator(separator);
    }
    return result;
  }

  private static Field<?> createStringJoin(AggregateField aggregateFieldConfiguration, String fromTable,
      String columnName, String columnAlias) {

    if (aggregateFieldConfiguration.getField()
        .isList()) {
      return createGroupConcat(aggregateFieldConfiguration, columnAlias);
    } else {
      String separator = aggregateFieldConfiguration.getSeparator();
      if (aggregateFieldConfiguration.isDistinct()) {
        return DSL.groupConcatDistinct(DSL.field(DSL.name(fromTable, columnName)))
            .separator(separator);
      } else {
        return DSL.groupConcat(DSL.field(DSL.name(fromTable, columnName)))
            .separator(separator);
      }
    }
  }

  private static Field<BigDecimal> bigDecimalField(String fromTable, String columnName) {
    return DSL.field(DSL.name(fromTable, columnName), BigDecimal.class);
  }

  private static Class<? extends Number> getNumericType(ScalarType scalarType) {
    switch (scalarType) {
      case INT:
        return Integer.class;
      case FLOAT:
        return BigDecimal.class;
      default:
        throw illegalArgumentException("Type {} is not supported", scalarType);
    }
  }
}
