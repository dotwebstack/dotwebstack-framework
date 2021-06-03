package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
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
import org.dotwebstack.framework.ext.spatial.GeometryFilterCriteria;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public final class FilterConditionHelper {

  private FilterConditionHelper() {}

  public static List<Condition> createFilterConditions(List<FilterCriteria> filterCriterias,
      ObjectSelectContext objectSelectContext, Table<?> fromTable) {
    return filterCriterias.stream()
        .map(filterCriteria -> {
          var filterTable =
              filterCriteria.isCompositeFilter() ? objectSelectContext.getTableAlias(filterCriteria.getFieldPath()[0])
                  : fromTable.getName();
          return createFilterCondition(filterCriteria, filterTable);
        })
        .collect(Collectors.toList());
  }

  private static Condition createFilterCondition(FilterCriteria filterCriteria, String fromTable) {
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
    } else if (filterCriteria instanceof GeometryFilterCriteria) {
      return createFilterCondition((GeometryFilterCriteria) filterCriteria, fromTable);
    }

    throw unsupportedOperationException("Filter '{}' is not supported!", filterCriteria.getClass()
        .getName());
  }

  private static Condition createFilterCondition(AndFilterCriteria andFilterCriteria, String fromTable) {
    var innerConditions = andFilterCriteria.getFilterCriterias()
        .stream()
        .map(innerCriteria -> createFilterCondition(innerCriteria, fromTable))
        .collect(Collectors.toList());

    return DSL.and(innerConditions);
  }

  private static Condition createFilterCondition(GeometryFilterCriteria geometryFilterCriteria, String fromTable) {
    Field<Object> field = getField(geometryFilterCriteria, fromTable);

    Field<?> geofilterField =
        DSL.field("ST_GeomFromText({0})", Object.class, DSL.val(geometryFilterCriteria.getGeometry()
            .toString()));

    switch (geometryFilterCriteria.getFilterOperator()) {
      case CONTAINS:
        return DSL.condition("ST_Contains({0}, {1})", field, geofilterField);
      case WITHIN:
        return DSL.condition("ST_Within({0}, {1})", geofilterField, field);
      case INTERSECTS:
        return DSL.condition("ST_Intersects({0}, {1})", field, geofilterField);
      default:
        throw illegalArgumentException("Unsupported geometry filter operation");
    }
  }

  private static Condition createFilterCondition(EqualsFilterCriteria equalsFilterCriteria, String fromTable) {
    Field<Object> field = getField(equalsFilterCriteria, fromTable);

    return field.eq(equalsFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(NotFilterCriteria notFilterCriteria, String fromTable) {
    var innerCondition = createFilterCondition(notFilterCriteria.getFilterCriteria(), fromTable);

    return DSL.not(innerCondition);
  }

  private static Condition createFilterCondition(GreaterThenFilterCriteria greaterThenFilterCriteria,
      String fromTable) {
    Field<Object> field = getField(greaterThenFilterCriteria, fromTable);

    return field.gt(greaterThenFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(GreaterThenEqualsFilterCriteria greaterThenEqualsFilterCriteria,
      String fromTable) {
    Field<Object> field = getField(greaterThenEqualsFilterCriteria, fromTable);

    return field.ge(greaterThenEqualsFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(LowerThenFilterCriteria lowerThenFilterCriteria, String fromTable) {
    Field<Object> field = getField(lowerThenFilterCriteria, fromTable);

    return field.lt(lowerThenFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(LowerThenEqualsFilterCriteria lowerThenEqualsFilterCriteria,
      String fromTable) {
    Field<Object> field = getField(lowerThenEqualsFilterCriteria, fromTable);

    return field.le(lowerThenEqualsFilterCriteria.getValue());
  }

  private static Condition createFilterCondition(InFilterCriteria inFilterCriteria, String fromTable) {
    Field<Object> field = getField(inFilterCriteria, fromTable);

    return field.in(inFilterCriteria.getValues());
  }

  private static Field<Object> getField(FilterCriteria filterCriteria, String fromTable) {
    var postgresFieldConfiguration = (PostgresFieldConfiguration) filterCriteria.getField();

    return DSL.field(DSL.name(fromTable, postgresFieldConfiguration.getColumn()));
  }
}
