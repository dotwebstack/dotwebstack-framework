package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.math.BigDecimal;
import java.util.function.Predicate;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class AggregateFieldHelper {

  public static final Predicate<AggregateField> isStringJoin =
      aggregateField -> JOIN.equals(aggregateField.getFunctionType());

  public static Field<?> create(AggregateField aggregateField, String fromTable, String columnName,
      String columnAlias) {
    Field<?> result;

    var aggregateFunction = aggregateField.getFunctionType();
    switch (aggregateFunction) {
      case AVG:
        result = DSL.avg(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateField.getType()));
        break;
      case COUNT:
        if (aggregateField.isDistinct()) {
          result = DSL.countDistinct(DSL.field(DSL.name(fromTable, columnName)));
        } else {
          result = DSL.count(DSL.field(DSL.name(fromTable, columnName)));
        }
        break;
      case JOIN:
        result = createStringJoin(aggregateField, fromTable, columnName, columnAlias);
        break;
      case MAX:
        result = DSL.max(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateField.getType()));
        break;
      case MIN:
        result = DSL.min(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateField.getType()));
        break;
      case SUM:
        result = DSL.sum(bigDecimalField(fromTable, columnName))
            .cast(getNumericType(aggregateField.getType()));
        break;
      default:
        throw illegalArgumentException("Aggregate function {} is not supported", aggregateField.getFunctionType());
    }
    return result;
  }

  private static Field<?> createGroupConcat(AggregateField aggregateField, String alias) {
    Field<?> result;
    String separator = aggregateField.getSeparator();
    if (aggregateField.isDistinct()) {
      result = DSL.groupConcatDistinct(DSL.field(DSL.name(alias)))
          .separator(separator);
    } else {
      result = DSL.groupConcat(DSL.field(DSL.name(alias)))
          .separator(separator);
    }
    return result;
  }

  private static Field<?> createStringJoin(AggregateField aggregateField, String fromTable, String columnName,
      String columnAlias) {

    if (aggregateField.getField()
        .isList()) {
      return createGroupConcat(aggregateField, columnAlias);
    } else {
      String separator = aggregateField.getSeparator();
      if (aggregateField.isDistinct()) {
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
