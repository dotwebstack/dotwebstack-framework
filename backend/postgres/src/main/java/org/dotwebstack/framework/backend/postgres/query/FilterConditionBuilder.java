package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getRequestedSrid;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getSridOfColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.JoinConfiguration.toJoinConfiguration;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINSANYOF;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINSAllOF;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.EQ;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.GT;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.GTE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.IN;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.LT;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.LTE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.MATCH;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.NOT;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;
import static org.dotwebstack.framework.ext.spatial.GeometryReader.readGeometry;
import static org.jooq.impl.DefaultDataType.getDefaultDataType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.EnumUtils;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
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
import org.jooq.util.postgres.PostgresDSL;
import org.locationtech.jts.geom.Geometry;

@Accessors(fluent = true)
@Setter
class FilterConditionBuilder {
  private static final char LIKE_ESCAPE_CHARACTER = '\\';

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
          .joinConfiguration(toJoinConfiguration(current))
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

    return createCondition(current, filterCriteria.getValue());
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

          return createConditions(field, filterCriteria.getValue());
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

  private Condition createConditions(Field<Object> field, Map<String, Object> values) {
    var conditions = values.entrySet()
        .stream()
        .map(entry -> {
          var filterOperator = EnumUtils.getEnumIgnoreCase(FilterOperator.class, entry.getKey());
          return createCondition(null, field, filterOperator, entry.getValue());
        })
        .collect(Collectors.toList());

    return andCondition(conditions);
  }

  private Condition createCondition(PostgresObjectField objectField, Map<String, Object> values) {
    var conditions = values.entrySet()
        .stream()
        .flatMap(entry -> {
          var filterOperator = EnumUtils.getEnumIgnoreCase(FilterOperator.class, entry.getKey());
          if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
            return createGeometryCondition(objectField, filterOperator, entry.getValue()).stream();
          }
          return Stream.of(createCondition(objectField, filterOperator, entry.getValue()));
        })
        .collect(Collectors.toList());

    return andCondition(conditions);
  }

  // private Condition createTermCondition(PostgresObjectField objectField, String operator, Object
  // value) {
  // Field<Object> field = DSL.field(DSL.name(table.getName(), objectField.getTsvColumn()));
  // if (FilterConstants.EQ_FIELD.equals(operator)) {
  // var queryString = DSL.val(Objects.toString(value));
  // var query = DSL.field("plainto_tsquery('simple',{0})", queryString);
  // return DSL.condition("{0} @@ {1}", field, query);
  // }
  //
  // if (FilterConstants.NOT_FIELD.equals(operator)) {
  // var conditions = castToMap(value).entrySet()
  // .stream()
  // .map(entry -> createTermCondition(objectField, entry.getKey(), entry.getValue()))
  // .collect(Collectors.toList());
  //
  // return DSL.not(andCondition(conditions));
  // }
  //
  // throw illegalArgumentException("Unknown filter field '%s'", operator);
  // }



  private Condition createCondition(PostgresObjectField objectField, FilterOperator operator, Object value) {
    if (NOT == operator) {
      var conditions = castToMap(value).entrySet()
          .stream()
          .map(entry -> {
            var filterOperator = EnumUtils.getEnumIgnoreCase(FilterOperator.class, entry.getKey());
            return createCondition(objectField, filterOperator, entry.getValue());
          })
          .collect(Collectors.toList());

      return DSL.not(andCondition(conditions));
    }

    if (objectField.isList()) {
      var field = DSL.field(DSL.name(table.getName(), objectField.getColumn()), Object[].class);
      var arrayValue = ObjectHelper.castToArray(value, objectField.getType());
      return createCondition(objectField, field, operator, arrayValue);
    } else {
      Field<Object> field = DSL.field(DSL.name(table.getName(), objectField.getColumn()));
      return createCondition(objectField, field, operator, value);
    }
  }

  private Condition createCondition(PostgresObjectField objectField, Field<Object[]> field, FilterOperator operator,
      Object[] value) {

    if (EQ == operator) {
      return field.eq(getArrayValue(objectField, value));
    }

    if (CONTAINSAllOF == operator) {
      return field.contains(getArrayValue(objectField, value));
    }

    if (CONTAINSANYOF == operator) {
      return PostgresDSL.arrayOverlap(field, getArrayValue(objectField, value));
    }

    throw illegalArgumentException("Unknown filter field '%s'", operator);
  }

  private Condition createCondition(PostgresObjectField objectField, Field<Object> field, FilterOperator operator,
      Object value) {
    if (MATCH == operator) {
      var escapedValue = escapeMatchValue(Objects.toString(value));
      return field.likeIgnoreCase(DSL.val(String.format("%%%s%%", escapedValue)))
          .escape(LIKE_ESCAPE_CHARACTER);
    }
    if (EQ == operator) {
      return field.eq(getValue(objectField, value));
    }

    if (LT == operator) {
      return field.lt(getValue(objectField, value));
    }

    if (LTE == operator) {
      return field.le(getValue(objectField, value));
    }

    if (GT == operator) {
      return field.gt(getValue(objectField, value));
    }

    if (GTE == operator) {
      return field.ge(getValue(objectField, value));
    }

    if (IN == operator) {
      return field.in(getFieldListValue(objectField, value));
    }

    throw illegalArgumentException("Unknown filter field '%s'", operator);
  }

  private List<Field<?>> getFieldListValue(PostgresObjectField objectField, Object listValue) {
    return castToList(listValue).stream()
        .map(value -> getValue(objectField, value))
        .collect(Collectors.toList());
  }

  private Field<Object> getArrayField(PostgresObjectField objectField, Object listValue) {
    var data = ObjectHelper.castToArray(listValue, objectField.getType());
    return DSL.val(data);
  }

  private Field<?> getValue(PostgresObjectField objectField, Object value) {
    if (objectField.isEnumeration()) {
      return getEnumerationValue(objectField, value);
    }
    return DSL.val(value);
  }

  private Field<Object[]> getArrayValue(PostgresObjectField objectField, Object[] value) {
    if (objectField.isEnumeration()) {
      return getEnumerationArrayValue(objectField, value);
    }
    return DSL.val(value);
  }

  // TODO: use optional construction?
  private Field<Object> getEnumerationValue(PostgresObjectField objectField, Object value) {
    var type = objectField.getEnumeration()
        .getType();
    var dataType = getDefaultDataType(SQLDialect.POSTGRES, type);
    var field = DSL.val(value);
    return field.cast(dataType);
  }

  private Field<Object[]> getEnumerationArrayValue(PostgresObjectField objectField, Object[] value) {
    if (objectField.isEnumeration()) {
      var type = objectField.getEnumeration()
          .getType();
      var dataType = getDefaultDataType(SQLDialect.POSTGRES, type);
      if (objectField.isList()) {
        // var field = getArrayField(objectField, value);
        var field = DSL.val(value);
        return field.cast(dataType.getArrayDataType());
      }
    }
    throw new IllegalArgumentException("TODO");
  }

  private Optional<Condition> createGeometryCondition(PostgresObjectField objectField, FilterOperator operator,
      Object value) {
    // TODO check this
    // if (ARGUMENT_SRID.equals(operator)) {
    // return Optional.empty();
    // }

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
      case CONTAINS:
        return Optional.of(DSL.condition("ST_Contains({0}, {1})", field, geoField));
      case WITHIN:
        return Optional.of(DSL.condition("ST_Within({0}, {1})", geoField, field));
      case INTERSECTS:
        return Optional.of(DSL.condition("ST_Intersects({0}, {1})", field, geoField));
      default:
        throw illegalArgumentException("Unsupported geometry filter operation");
    }
  }

  private Condition andCondition(List<Condition> conditions) {
    return conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);
  }

  private String escapeMatchValue(String inputValue) {
    String result = inputValue.replace(String.valueOf(LIKE_ESCAPE_CHARACTER),
        String.valueOf(new char[] {LIKE_ESCAPE_CHARACTER, LIKE_ESCAPE_CHARACTER}));
    result = result.replace("_", LIKE_ESCAPE_CHARACTER + "_");
    result = result.replace("%", LIKE_ESCAPE_CHARACTER + "%");
    return result;
  }
}
