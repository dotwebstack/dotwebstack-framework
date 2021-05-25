package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.core.query.model.filter.AndFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.InFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public final class FilterConditionHelper {

  private FilterConditionHelper() {}

  public static List<Condition> createFilterConditions(List<FilterCriteria> filterCriterias, Table<?> fromTable) {
    return filterCriterias.stream()
        .map(filterCriteria -> createFilterCondition(filterCriteria, fromTable))
        .collect(Collectors.toList());
  }

  private static Condition createFilterCondition(FilterCriteria filterCriteria, Table<?> fromTable) {
    if (filterCriteria instanceof EqualsFilterCriteria) {
      return createFilterCondition((EqualsFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof NotFilterCriteria) {
      return createFilterCondition((NotFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof GreaterThenFilterCriteria) {
      return createFilterCondition((GreaterThenFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof GreaterThenEqualsFilterCriteria) {
      return createFilterCondition((GreaterThenEqualsFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof LowerThenFilterCriteria) {
      return createFilterCondition((LowerThenFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof LowerThenEqualsFilterCriteria) {
      return createFilterCondition((LowerThenEqualsFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof InFilterCriteria) {
      return createFilterCondition((InFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof AndFilterCriteria) {
      return createFilterCondition((AndFilterCriteria) filterCriteria, fromTable);
    }

    throw unsupportedOperationException("Filter '{}' is not supported!", filterCriteria.getClass()
        .getName());
  }

  private static Condition createFilterCondition(AndFilterCriteria andFilterCriteria, Table<?> fromTable) {
    var innerConditions = andFilterCriteria.getFilterCriterias()
        .stream()
        .map(innerCriteria -> createFilterCondition(innerCriteria, fromTable))
        .collect(Collectors.toList());

    return DSL.and(innerConditions);
  }

  private static Condition createFilterCondition(EqualsFilterCriteria equalsFilterCriteria, Table<?> fromTable) {
    Field<Object> field = getField(equalsFilterCriteria, fromTable);

    return field.eq(equalsFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(NotFilterCriteria notFilterCriteria, Table<?> fromTable) {
    var innerCondition = createFilterCondition(notFilterCriteria.getFilterCriteria(), fromTable);

    return DSL.not(innerCondition);
  }

  private static Condition createFilterCondition(GreaterThenFilterCriteria greaterThenFilterCriteria,
      Table<?> fromTable) {
    Field<Object> field = getField(greaterThenFilterCriteria, fromTable);

    return field.gt(greaterThenFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(GreaterThenEqualsFilterCriteria greaterThenEqualsFilterCriteria,
      Table<?> fromTable) {
    Field<Object> field = getField(greaterThenEqualsFilterCriteria, fromTable);

    return field.ge(greaterThenEqualsFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(LowerThenFilterCriteria lowerThenFilterCriteria, Table<?> fromTable) {
    Field<Object> field = getField(lowerThenFilterCriteria, fromTable);

    return field.lt(lowerThenFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(LowerThenEqualsFilterCriteria lowerThenEqualsFilterCriteria,
      Table<?> fromTable) {
    Field<Object> field = getField(lowerThenEqualsFilterCriteria, fromTable);

    return field.le(lowerThenEqualsFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(InFilterCriteria inFilterCriteria, Table<?> fromTable) {
    Field<Object> field = getField(inFilterCriteria, fromTable);

    return field.in(inFilterCriteria.getValues());
  }

  private static Field<Object> getField(FilterCriteria filterCriteria, Table<?> fromTable) {
    var postgresFieldConfiguration = (PostgresFieldConfiguration) filterCriteria.getField();

    return DSL.field(DSL.name(fromTable.getName(), postgresFieldConfiguration.getColumn()));
  }
}
