package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class FilterConditionHelper {

  public static List<Condition> createFilterConditions(List<FilterCriteria> filterCriterias, Table<?> fromTable) {
    return filterCriterias.stream()
        .map(filterCriteria -> createFilterCondition(filterCriteria, fromTable))
        .collect(Collectors.toList());
  }

  private static Condition createFilterCondition(FilterCriteria filterCriteria, Table<?> fromTable) {
    if (filterCriteria instanceof EqualsFilterCriteria) {
      return createFilterCondition((EqualsFilterCriteria) filterCriteria, fromTable);
    }

    throw unsupportedOperationException("Filter '{}' is not supported!", filterCriteria.getClass()
        .getName());
  }

  private static Condition createFilterCondition(EqualsFilterCriteria equalsFilterCriteria, Table<?> fromTable) {
    PostgresFieldConfiguration postgresFieldConfiguration =
        (PostgresFieldConfiguration) equalsFilterCriteria.getField();

    Field<Object> field = DSL.field(DSL.name(fromTable.getName(), postgresFieldConfiguration.getColumn()));

    return field.eq(equalsFilterCriteria.getValue());
  }

}
