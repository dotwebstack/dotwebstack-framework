package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
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
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public final class FilterConditionHelper {

  private final DSLContext dslContext;

  public FilterConditionHelper(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  public void createFilterConditions(List<FilterCriteria> filterCriterias, SelectQuery<?> query, Table<?> fromTable, PostgresTypeConfiguration mainTypeConfiguration) {
    List<Condition> filterConditions = new ArrayList<>();
    filterCriterias.forEach(filterCriteria -> {
      // ALLEEN indien isCompositeFilter
      var isCompositeFilter = true;
      if(isCompositeFilter) {
        var filterFields = filterCriteria.getFilterFields();
        var filterTable = createFilterJoin(filterFields, fromTable, mainTypeConfiguration, query);
        filterConditions.add(createFilterCondition(filterCriteria, filterTable));
      } else {
        filterConditions.add(createFilterCondition(filterCriteria, fromTable));
      }

    });
    query.addConditions(filterConditions);
  }

  public List<Condition> createFilterConditions(List<FilterCriteria> filterCriterias, Table<?> fromTable) {
    return filterCriterias.stream()
        .map(filterCriteria -> createFilterCondition(filterCriteria, fromTable))
        .collect(Collectors.toList());
  }

  public Condition createFilterCondition(FilterCriteria filterCriteria, Table<?> fromTable) {
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

  private Table<?> createFilterJoin(String[] fields,  Table<?> fromTable, PostgresTypeConfiguration typeConfiguration, SelectQuery<?> query) {
    var filterTable = fromTable;
    var field = fields[0];
    var fieldConfiguration = typeConfiguration.getFields().get(field);
    if(fieldConfiguration.isObjectField()) {
      // TODO: table alias en column alias(?)
      filterTable = findTable(((PostgresTypeConfiguration)fieldConfiguration.getTypeConfiguration()).getTable()).asTable("fjt_1");
      // TODO: use joinColumn
      var leftColumn = fromTable.field(fieldConfiguration.getJoinColumns().get(0).getName(), Object.class);
      var rightColumn = filterTable.field(fieldConfiguration.getJoinColumns().get(0).getReferencedField());
      var joinCondition = Objects.requireNonNull(leftColumn).eq(rightColumn);
      query.addJoin(filterTable, JoinType.JOIN, joinCondition);
      fields = Arrays.copyOfRange(fields, 1, fields.length);
      createFilterJoin(fields, filterTable, (PostgresTypeConfiguration) fieldConfiguration.getTypeConfiguration(), query);
    }
    return filterTable;

  }

  private Table<?> findTable(String name) {
    var path = name.split("\\.");
    var tables = dslContext.meta()
        .getTables(path[path.length - 1]);

    return tables.get(0);
  }

  private Condition createFilterCondition(AndFilterCriteria andFilterCriteria, Table<?> fromTable) {
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

  private Condition createFilterCondition(NotFilterCriteria notFilterCriteria, Table<?> fromTable) {
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
