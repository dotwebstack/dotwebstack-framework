package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getRequestedSrid;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getSridOfColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;
import static org.dotwebstack.framework.ext.spatial.GeometryReader.readGeometry;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_SRID;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
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
  private FilterCriteria filterCriteria;

  private ContextCriteria contextCriteria;

  @NotNull
  private Table<Record> table;

  private FilterConditionBuilder() {}

  static FilterConditionBuilder newFiltering() {
    return new FilterConditionBuilder();
  }

  Condition build() {
    validateFields(this);

    return walkFieldPath(filterCriteria);
  }

  private Condition walkFieldPath(FilterCriteria filterCriteria) {
    var fieldPath = filterCriteria.getFieldPath();
    var current = (PostgresObjectField) fieldPath.get(0);

    if (fieldPath.size() > 1) {
      var childCriteria = FilterCriteria.builder()
          .filterType(filterCriteria.getFilterType())
          .fieldPath(fieldPath.subList(1, fieldPath.size()))
          .value(filterCriteria.getValue())
          .build();

      if (current.getTargetType()
          .isNested()) {
        return walkFieldPath(childCriteria);
      }

      var filterTable = findTable(((PostgresObjectType) current.getTargetType()).getTable(), contextCriteria)
          .as(aliasManager.newAlias());

      var filterQuery = dslContext.selectQuery(filterTable);

      filterQuery.addSelect(DSL.val(1));

      newJoin().table(table)
          .current(current)
          .tableCreator(createTableCreator(filterQuery, contextCriteria, aliasManager))
          .relatedTable(filterTable)
          .build()
          .forEach(filterQuery::addConditions);

      var nestedCondition = newFiltering().aliasManager(aliasManager)
          .contextCriteria(contextCriteria)
          .table(filterTable)
          .filterCriteria(childCriteria)
          .build();

      filterQuery.addConditions(nestedCondition);

      return DSL.exists(filterQuery);
    }

    return createCondition(current, filterCriteria.getFilterType(), filterCriteria.getValue());
  }

  private Condition createCondition(PostgresObjectField objectField, FilterType filterType,
      Map<String, Object> values) {
    if (FilterType.EXACT.equals(filterType)) {
      var conditions = values.entrySet()
          .stream()
          .flatMap(entry -> createExactCondition(objectField, entry.getKey(), entry.getValue()).stream())
          .collect(Collectors.toList());

      return andCondition(conditions);
    }

    if (FilterType.TERM.equals(filterType)) {
      var conditions = values.entrySet()
          .stream()
          .map(entry -> createTermCondition(objectField, entry.getKey(), entry.getValue()))
          .collect(Collectors.toList());

      return andCondition(conditions);
    }

    throw unsupportedOperationException("Unknown filtertype '{}'", filterType);
  }

  private Condition createTermCondition(PostgresObjectField objectField, String operator, Object value) {
    Field<Object> field = DSL.field(DSL.name(table.getName(), objectField.getTsvColumn()));
    if (FilterConstants.EQ_FIELD.equals(operator)) {
      var queryString = DSL.val(Objects.toString(value));
      var query = DSL.field("plainto_tsquery('simple',{0})", queryString);
      return DSL.condition("{0} @@ {1}", field, query);
    }

    if (FilterConstants.NOT_FIELD.equals(operator)) {
      var conditions = castToMap(value).entrySet()
          .stream()
          .map(entry -> createTermCondition(objectField, entry.getKey(), entry.getValue()))
          .collect(Collectors.toList());

      return DSL.not(andCondition(conditions));
    }

    throw illegalArgumentException("Unknown filter filterField '%s'", operator);
  }

  private Optional<Condition> createExactCondition(PostgresObjectField objectField, String operator, Object value) {
    Field<Object> field = DSL.field(DSL.name(table.getName(), objectField.getColumn()));

    if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
      return createGeometryCondition(operator, objectField, value);
    }

    if (FilterConstants.EQ_FIELD.equals(operator)) {
      return Optional.of(field.eq(DSL.val(value)));
    }

    if (FilterConstants.LT_FIELD.equals(operator)) {
      return Optional.of(field.lt(DSL.val(value)));
    }

    if (FilterConstants.LTE_FIELD.equals(operator)) {
      return Optional.of(field.le(DSL.val(value)));
    }

    if (FilterConstants.GT_FIELD.equals(operator)) {
      return Optional.of(field.gt(DSL.val(value)));
    }

    if (FilterConstants.GTE_FIELD.equals(operator)) {
      return Optional.of(field.ge(DSL.val(value)));
    }

    if (FilterConstants.IN_FIELD.equals(operator)) {
      return Optional.of(field.in(castToList(value)));
    }

    if (FilterConstants.NOT_FIELD.equals(operator)) {
      var conditions = castToMap(value).entrySet()
          .stream()
          .flatMap(entry -> createExactCondition(objectField, entry.getKey(), entry.getValue()).stream())
          .collect(Collectors.toList());

      return Optional.of(DSL.not(andCondition(conditions)));
    }

    throw illegalArgumentException("Unknown filter filterField '%s'", operator);
  }


  private Optional<Condition> createGeometryCondition(String operator, PostgresObjectField objectField, Object value) {
    if (ARGUMENT_SRID.equals(operator)) {
      return Optional.empty();
    }

    var mapValue = ObjectHelper.castToMap(value);

    var requestedSrid = getRequestedSrid(mapValue);

    var columnName = getColumnName(objectField.getSpatial(), requestedSrid);
    var field = DSL.field(DSL.name(table.getName(), columnName));

    var geometry = readGeometry(mapValue);
    var columnSrid = getSridOfColumnName(objectField.getSpatial(), columnName);
    geometry.setSRID(columnSrid);

    Field<Geometry> geoField = DSL.val(geometry)
        .cast(GEOMETRY_DATATYPE);

    switch (operator) {
      case SpatialConstants.CONTAINS:
        return Optional.of(DSL.condition("ST_Contains({0}, {1})", field, geoField));
      case SpatialConstants.WITHIN:
        return Optional.of(DSL.condition("ST_Within({0}, {1})", geoField, field));
      case SpatialConstants.INTERSECTS:
        return Optional.of(DSL.condition("ST_Intersects({0}, {1})", field, geoField));
      default:
        throw illegalArgumentException("Unsupported geometry filter operation");
    }
  }

  private Condition andCondition(List<Condition> conditions) {
    return conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);
  }
}
