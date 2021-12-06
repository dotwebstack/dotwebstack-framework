package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getRequestedSrid;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getSridOfColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
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
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.dotwebstack.framework.core.model.ObjectField;
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
      var childCriteria = createChildCriteria(filterCriteria.getFilterType(), fieldPath, filterCriteria.getValue());

      if (current.getTargetType()
          .isNested()) {
        if (JoinHelper.hasNestedReference(current)) {
          return createConditionsForMatchingNestedReference(current, fieldPath);
        }

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

  private Condition createConditionsForMatchingNestedReference(PostgresObjectField objectField,
      List<ObjectField> fieldPath) {
    String referencedField = toFieldPathString(fieldPath.subList(1, fieldPath.size()));

    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return andCondition(
          createConditionsForMatchingNestedReference(objectField.getJoinColumns(), referencedField, table.getName()));
    }

    if (objectField.getJoinTable() != null) {
      return createConditionsForMatchingNestedReference(objectField, referencedField);
    }

    throw illegalArgumentException("ObjectField '{}' in ObjectType '{}' has no join configuration",
        objectField.getName(), objectField.getObjectType()
            .getName());
  }

  private List<Condition> createConditionsForMatchingNestedReference(List<JoinColumn> joinColumns,
      String referencedField, String tableName) {
    return joinColumns.stream()
        .filter(joinColumn -> referencedField.equals(joinColumn.getReferencedField()))
        .map(joinColumn -> {
          var field = DSL.field(DSL.name(tableName, joinColumn.getName()));

          return createExactConditions(field, filterCriteria.getValue());
        })
        .collect(Collectors.toList());
  }

  private Condition createConditionsForMatchingNestedReference(PostgresObjectField current, String referencedField) {
    var joinTable = findTable(current.getJoinTable()
        .getName(), contextCriteria);

    var leftSide = createJoinConditions(joinTable, table, current.getJoinTable()
        .getJoinColumns(), (PostgresObjectType) current.getObjectType());

    var filterQuery = dslContext.selectQuery(joinTable);

    filterQuery.addConditions(leftSide);

    filterQuery.addSelect(DSL.val(1));

    createConditionsForMatchingNestedReference(current.getJoinTable()
        .getInverseJoinColumns(), referencedField, joinTable.getName()).forEach(filterQuery::addConditions);

    return DSL.exists(filterQuery);
  }

  private String toFieldPathString(List<ObjectField> fieldPath) {
    return fieldPath.stream()
        .map(ObjectField::getName)
        .collect(Collectors.joining("."));
  }

  private FilterCriteria createChildCriteria(FilterType filterType, List<ObjectField> fieldPath,
      Map<String, Object> value) {
    return FilterCriteria.builder()
        .filterType(filterType)
        .fieldPath(fieldPath.subList(1, fieldPath.size()))
        .value(value)
        .build();
  }

  private Condition createCondition(PostgresObjectField objectField, FilterType filterType,
      Map<String, Object> values) {
    if (FilterType.EXACT.equals(filterType)) {
      var conditions = values.entrySet()
          .stream()
          .flatMap(entry -> {
            if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
              return createExactGeometryCondition(objectField, entry.getKey(), entry.getValue()).stream();
            }
            return Stream.of(createExactCondition(objectField, entry.getKey(), entry.getValue()));
          })
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

    throw illegalArgumentException("Unknown filter field '%s'", operator);
  }

  private Condition createExactConditions(Field<Object> field, Map<String, Object> values) {
    var conditions = values.entrySet()
        .stream()
        .map(entry -> createExactCondition(field, entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    return andCondition(conditions);
  }

  private Condition createExactCondition(PostgresObjectField objectField, String operator, Object value) {
    Field<Object> field = DSL.field(DSL.name(table.getName(), objectField.getColumn()));

    return createExactCondition(field, operator, value);
  }

  private Condition createExactCondition(Field<Object> field, String operator, Object value) {
    if (FilterConstants.EQ_FIELD.equals(operator)) {
      return field.eq(DSL.val(value));
    }

    if (FilterConstants.LT_FIELD.equals(operator)) {
      return field.lt(DSL.val(value));
    }

    if (FilterConstants.LTE_FIELD.equals(operator)) {
      return field.le(DSL.val(value));
    }

    if (FilterConstants.GT_FIELD.equals(operator)) {
      return field.gt(DSL.val(value));
    }

    if (FilterConstants.GTE_FIELD.equals(operator)) {
      return field.ge(DSL.val(value));
    }

    if (FilterConstants.IN_FIELD.equals(operator)) {
      return field.in(castToList(value));
    }

    if (FilterConstants.NOT_FIELD.equals(operator)) {
      var conditions = castToMap(value).entrySet()
          .stream()
          .map(entry -> createExactCondition(field, entry.getKey(), entry.getValue()))
          .collect(Collectors.toList());

      return DSL.not(andCondition(conditions));
    }

    throw illegalArgumentException("Unknown filter field '%s'", operator);
  }

  private Optional<Condition> createExactGeometryCondition(PostgresObjectField objectField, String operator,
      Object value) {
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
