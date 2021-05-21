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
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.SEPARATOR_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.SelectedField;
import java.math.BigDecimal;
import java.util.Optional;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class AggregateFieldFactory {
  private static final String DEFAULT_SEPARATOR = ",";

  public Field<?> create(AggregateFieldConfiguration aggregateFieldConfiguration, String fromTable, String columnName,
      String columnAlias) {
    Field<?> result;

    var aggregateFunction = aggregateFieldConfiguration.getAggregateFunctionType();
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
            aggregateFieldConfiguration.getAggregateFunctionType());
    }
    return result;
  }

  public Field<?> create(PostgresFieldConfiguration aggregateFieldConfiguration, SelectedField selectedField, // NOSONAR
      String fromTable, String columnName, String columnAlias) {
    Field<?> result;
    String aggregateFunction = selectedField.getName();

    switch (aggregateFunction) {
      case COUNT_FIELD:
        if (isDistinct(selectedField)) {
          result = DSL.countDistinct(DSL.field(DSL.name(fromTable, columnName)));
        } else {
          result = DSL.count(DSL.field(DSL.name(fromTable, columnName)));
        }
        break;
      case STRING_JOIN_FIELD:
        result = createStringJoin(aggregateFieldConfiguration, selectedField, fromTable, columnName, columnAlias);
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

  private Field<?> createGroupConcat(SelectedField selectedField, String alias) {
    Field<?> result;
    String separator = getSeparator(selectedField);
    if (isDistinct(selectedField)) {
      result = DSL.groupConcatDistinct(DSL.field(DSL.name(alias)))
          .separator(separator);
    } else {
      result = DSL.groupConcat(DSL.field(DSL.name(alias)))
          .separator(separator);
    }
    return result;
  }

  private Field<?> createGroupConcat(AggregateFieldConfiguration aggregateFieldConfiguration, String alias) {
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

  private Field<?> createStringJoin(PostgresFieldConfiguration aggregateFieldConfiguration, SelectedField selectedField,
      String fromTable, String columnName, String columnAlias) {
    if (aggregateFieldConfiguration.isList()) {
      return createGroupConcat(selectedField, columnAlias);
    } else {
      String separator = getSeparator(selectedField);
      if (isDistinct(selectedField)) {
        return DSL.groupConcatDistinct(DSL.field(DSL.name(fromTable, columnName)))
            .separator(separator);
      } else {
        return DSL.groupConcat(DSL.field(DSL.name(fromTable, columnName)))
            .separator(separator);
      }
    }
  }

  private Field<?> createStringJoin(AggregateFieldConfiguration aggregateFieldConfiguration, String fromTable,
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

  private boolean isDistinct(SelectedField selectedField) {
    return Optional.ofNullable((Boolean) selectedField.getArguments()
        .get(DISTINCT_ARGUMENT))
        .orElse(Boolean.FALSE);
  }

  private String getSeparator(SelectedField selectedField) {
    return Optional.ofNullable((String) selectedField.getArguments()
        .get(SEPARATOR_ARGUMENT))
        .orElse(DEFAULT_SEPARATOR);
  }

  private Field<BigDecimal> bigDecimalField(String fromTable, String columnName) {
    return DSL.field(DSL.name(fromTable, columnName), BigDecimal.class);
  }


  private Class<? extends Number> getNumericType(ScalarType scalarType) {
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
