package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.ext.spatial.GeometryReader;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.locationtech.jts.geom.Geometry;

@Accessors(fluent = true)
@Setter
class FilterConditionBuilder {

  private static final DataType<Geometry> GEOMETRY_DATATYPE =
      new DefaultDataType<>(SQLDialect.POSTGRES, Geometry.class, "geometry");

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  @NotNull
  private AliasManager aliasManager;

  @NotNull
  private List<FilterCriteria> filterCriterias;

  @NotNull
  private ObjectRequest objectRequest;

  @NotNull
  private Table<Record> table;

  private FilterConditionBuilder() {}

  static FilterConditionBuilder newFiltering() {
    return new FilterConditionBuilder();
  }

  List<Condition> build() {
    validateFields(this);

    return filterCriterias.stream()
        .map(filterCriteria -> createFilterCondition(objectRequest, filterCriteria.getFieldPath()
            .stream()
            .map(PostgresObjectField.class::cast)
            .collect(Collectors.toList()), filterCriteria.getValue(), table))
        .collect(Collectors.toList());
  }

  private Condition createFilterCondition(ObjectRequest objectRequest, List<PostgresObjectField> fieldPath,
      Map<String, Object> value, Table<Record> table) {
    var current = fieldPath.get(0);

    if (fieldPath.size() > 1) {
      var rest = fieldPath.subList(1, fieldPath.size());

      if (current.getTargetType()
          .isNested()) {
        return createFilterCondition(objectRequest, rest, value, table);
      }

      var filterTable =
          findTable(((PostgresObjectType) current.getTargetType()).getTable(), objectRequest.getContextCriteria())
              .as(aliasManager.newAlias());

      var filterQuery = dslContext.selectQuery(filterTable);

      filterQuery.addSelect(DSL.val(1));

      JoinBuilder.newJoin()
          .table(table)
          .current(current)
          .relatedTable(filterTable)
          .build()
          .forEach(filterQuery::addConditions);

      var nestedCondition = createFilterCondition(objectRequest, rest, value, filterTable);

      filterQuery.addConditions(nestedCondition);

      return DSL.exists(filterQuery);
    }

    var conditions = value.entrySet()
        .stream()
        .map(entry -> createFilterCondition(entry.getKey(), current, entry.getValue()))
        .collect(Collectors.toList());

    return conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);
  }

  @SuppressWarnings("unchecked")
  private Condition createFilterCondition(String filterField, PostgresObjectField objectField, Object value) {
    Field<Object> field = DSL.field(objectField.getColumn());

    if (FilterConstants.EQ_FIELD.equals(filterField)) {
      return field.eq(DSL.val(value));
    }

    if (FilterConstants.LT_FIELD.equals(filterField)) {
      return field.lt(DSL.val(value));
    }

    if (FilterConstants.LTE_FIELD.equals(filterField)) {
      return field.le(DSL.val(value));
    }

    if (FilterConstants.GT_FIELD.equals(filterField)) {
      return field.gt(DSL.val(value));
    }

    if (FilterConstants.GTE_FIELD.equals(filterField)) {
      return field.ge(DSL.val(value));
    }

    if (FilterConstants.IN_FIELD.equals(filterField)) {
      List<Object> list = (List<Object>) value;
      return field.in(list);
    }

    if (FilterConstants.NOT_FIELD.equals(filterField)) {
      return createNotCondition(objectField, (Map<String, Object>) value);
    }

    if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
      return createGeometryCondition(filterField, (Map<String, String>) value, field);
    }

    throw illegalArgumentException("Unknown filter filterField '%s'", filterField);
  }

  private Condition createNotCondition(PostgresObjectField objectField, Map<String, Object> value) {
    var conditions = value.entrySet()
        .stream()
        .map(entry -> createFilterCondition(entry.getKey(), objectField, entry.getValue()))
        .collect(Collectors.toList());

    var condition = conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);

    return DSL.not(condition);
  }

  private Condition createGeometryCondition(String filterField, Map<String, String> value, Field<Object> field) {
    Geometry geometry = GeometryReader.readGeometry(value);

    Field<Geometry> geoField = DSL.val(geometry)
        .cast(GEOMETRY_DATATYPE);

    switch (filterField) {
      case SpatialConstants.CONTAINS:
        return DSL.condition("ST_Contains({0}, {1})", field, geoField);
      case SpatialConstants.WITHIN:
        return DSL.condition("ST_Within({0}, {1})", geoField, field);
      case SpatialConstants.INTERSECTS:
        return DSL.condition("ST_Intersects({0}, {1})", field, geoField);
      default:
        throw illegalArgumentException("Unsupported geometry filter operation");
    }
  }
}