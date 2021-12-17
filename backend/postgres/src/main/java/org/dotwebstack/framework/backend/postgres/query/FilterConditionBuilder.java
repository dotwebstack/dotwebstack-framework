package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getRequestedSrid;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getSridOfColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.JoinConfiguration.toJoinConfiguration;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.andCondition;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINS_ALL_OF;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINS_ANY_OF;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.EQ;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.GT;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.GTE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.IN;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.LT;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.LTE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.MATCH;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.NOT;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.SRID;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
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
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.filter.ScalarFieldFilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.dotwebstack.framework.core.model.AbstractObjectField;
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
  private static final String ERROR_MESSAGE = "Unknown filter field '%s'";

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

    return build(filterCriteria);
  }

  private Condition build(FilterCriteria filterCriteria) {
    if (filterCriteria.isGroupFilter()) {
      var group = filterCriteria.asGroupFilter();

      var conditions = group.getFilterCriterias()
          .stream()
          .map(this::build)
          .collect(Collectors.toList());

      if (conditions.size() > 1) {
        switch (group.getLogicalOperator()) {
          case AND:
            return DSL.and(conditions);
          case OR:
            return DSL.or(conditions);
          default:
            throw unsupportedOperationException("Logical operator '{}' is not supported!", group.getLogicalOperator());
        }
      }

      if (conditions.size() == 1) {
        return conditions.get(0);
      }

      return null;
    }

    if (filterCriteria.isScalarFieldFilter()) {
      return walkFieldPath(filterCriteria.asScalarFieldFilter());
    }

    throw unsupportedOperationException("Filter criteria '{}' is not supported!", filterCriteria.getClass()
        .getSimpleName());
  }

  private Condition walkFieldPath(ScalarFieldFilterCriteria filterCriteria) {
    var fieldPath = filterCriteria.getFieldPath();
    var current = (PostgresObjectField) fieldPath.get(0);

    if (fieldPath.size() > 1) {
      var childCriteria = createChildCriteria(filterCriteria.getFilterType(), fieldPath, filterCriteria.getValue());

      if (current.getTargetType()
          .isNested()) {
        if (JoinHelper.hasNestedReference(current)) {
          return createConditionsForMatchingNestedReference(filterCriteria, current, fieldPath);
        }

        return walkFieldPath(childCriteria);
      }

      var filterTable = findTable(((PostgresObjectType) current.getTargetType()).getTable(), contextCriteria)
          .as(aliasManager.newAlias());

      var filterQuery = dslContext.selectQuery(filterTable);

      filterQuery.addSelect(DSL.val(1));

      var joinConditions = newJoin().table(table)
          .joinConfiguration(toJoinConfiguration(current))
          .tableCreator(createTableCreator(filterQuery, contextCriteria, aliasManager))
          .relatedTable(filterTable)
          .build();

      filterQuery.addConditions(joinConditions);

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

  private Condition createConditionsForMatchingNestedReference(ScalarFieldFilterCriteria filterCriteria,
      PostgresObjectField objectField, List<ObjectField> fieldPath) {
    String referencedField = toFieldPathString(fieldPath.subList(1, fieldPath.size()));

    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return andCondition(createConditionsForMatchingNestedReference(filterCriteria, objectField.getJoinColumns(),
          referencedField, table.getName()));
    }

    if (objectField.getJoinTable() != null) {
      return createConditionsForMatchingNestedReference(filterCriteria, objectField, referencedField);
    }

    throw illegalArgumentException("ObjectField '{}' in ObjectType '{}' has no join configuration",
        objectField.getName(), objectField.getObjectType()
            .getName());
  }

  private List<Condition> createConditionsForMatchingNestedReference(ScalarFieldFilterCriteria filterCriteria,
      List<JoinColumn> joinColumns, String referencedField, String tableName) {
    return joinColumns.stream()
        .filter(joinColumn -> referencedField.equals(joinColumn.getReferencedField()))
        .map(joinColumn -> {
          var field = DSL.field(DSL.name(tableName, joinColumn.getName()));

          return createConditions(field, filterCriteria.getValue());
        })
        .collect(Collectors.toList());
  }

  private Condition createConditionsForMatchingNestedReference(ScalarFieldFilterCriteria filterCriteria,
      PostgresObjectField current, String referencedField) {
    var joinTable = findTable(current.getJoinTable()
        .getName(), contextCriteria);

    var leftSide = createJoinConditions(joinTable, table, current.getJoinTable()
        .getJoinColumns(), (PostgresObjectType) current.getObjectType());

    var filterQuery = dslContext.selectQuery(joinTable);

    filterQuery.addConditions(leftSide);

    filterQuery.addSelect(DSL.val(1));

    createConditionsForMatchingNestedReference(filterCriteria, current.getJoinTable()
        .getInverseJoinColumns(), referencedField, joinTable.getName()).forEach(filterQuery::addConditions);

    return DSL.exists(filterQuery);
  }

  private String toFieldPathString(List<ObjectField> fieldPath) {
    return fieldPath.stream()
        .map(ObjectField::getName)
        .collect(Collectors.joining("."));
  }

  private ScalarFieldFilterCriteria createChildCriteria(FilterType filterType, List<ObjectField> fieldPath,
      Map<String, Object> value) {
    return ScalarFieldFilterCriteria.builder()
        .filterType(filterType)
        .fieldPath(fieldPath.subList(1, fieldPath.size()))
        .value(value)
        .build();
  }

  private Condition createConditions(Field<Object> field, Map<String, Object> values) {
    var conditions = values.entrySet()
        .stream()
        .map(entry -> {
          var filterOperator = FilterOperator.getFilterOperator(entry.getKey());
          return createCondition(null, field, filterOperator, entry.getValue());
        })
        .collect(Collectors.toList());

    return andCondition(conditions);
  }

  private Condition createCondition(PostgresObjectField objectField, Map<String, Object> values) {
    var conditions = values.entrySet()
        .stream()
        .flatMap(entry -> Optional.ofNullable(FilterOperator.getFilterOperator(entry.getKey()))
            .map(filterOperator -> {
              if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
                return createGeometryCondition(objectField, filterOperator, entry.getValue()).stream();
              }
              return Stream.of(createCondition(objectField, filterOperator, entry.getValue()));
            })
            .orElseThrow(() -> illegalArgumentException(ERROR_MESSAGE, entry.getKey())))
        .collect(Collectors.toList());

    return andCondition(conditions);

  }

  private Condition createCondition(PostgresObjectField objectField, FilterOperator operator, Object value) {
    if (NOT == operator) {
      var conditions = castToMap(value).entrySet()
          .stream()
          .map(entry -> Optional.ofNullable(FilterOperator.getFilterOperator(entry.getKey()))
              .map(filterOperator -> createCondition(objectField, filterOperator, entry.getValue()))
              .orElseThrow(() -> illegalArgumentException(ERROR_MESSAGE, entry.getKey())))
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

    if (CONTAINS_ALL_OF == operator) {
      return field.contains(getArrayValue(objectField, value));
    }

    if (CONTAINS_ANY_OF == operator) {
      return PostgresDSL.arrayOverlap(field, getArrayValue(objectField, value));
    }

    throw illegalArgumentException(ERROR_MESSAGE, operator);
  }

  private Condition createCondition(PostgresObjectField objectField, Field<Object> field, FilterOperator operator,
      Object value) {
    if (MATCH == operator) {
      var escapedValue = escapeMatchValue(Objects.toString(value));
      return field.likeIgnoreCase(DSL.val(String.format("%%%s%%", escapedValue)))
          .escape(LIKE_ESCAPE_CHARACTER);
    }
    if (EQ == operator) {
      if (value == null) {
        return field.isNull();
      } else {
        return field.eq(getValue(objectField, value));
      }
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

    throw illegalArgumentException(ERROR_MESSAGE, operator);
  }

  private List<Field<?>> getFieldListValue(PostgresObjectField objectField, Object listValue) {
    return castToList(listValue).stream()
        .map(value -> getValue(objectField, value))
        .collect(Collectors.toList());
  }

  private Field<?> getValue(PostgresObjectField objectField, Object value) {
    if (value == null) {
      return DSL.param(createDataType(Boolean.class));
    }

    var field = DSL.val(value);

    return Optional.ofNullable(objectField)
        .map(AbstractObjectField::getEnumeration)
        .map(FieldEnumConfiguration::getType)
        .map(type -> {
          var dataType = getDefaultDataType(SQLDialect.POSTGRES, type);
          return field.cast(dataType);
        })
        .orElse(field);
  }

  private DataType<?> createDataType(Class<?> dataType) {
    return DefaultDataType.getDataType(SQLDialect.POSTGRES, dataType);
  }

  private Field<Object[]> getArrayValue(PostgresObjectField objectField, Object[] value) {
    if (objectField.isEnumeration()) {
      return getEnumerationArrayValue(objectField, value);
    }
    return DSL.val(value);
  }

  private Field<Object[]> getEnumerationArrayValue(PostgresObjectField objectField, Object[] value) {
    return Optional.ofNullable(objectField.getEnumeration())
        .map(FieldEnumConfiguration::getType)
        .map(type -> {
          var dataType = getDefaultDataType(SQLDialect.POSTGRES, type);
          var field = DSL.val(value);
          return field.cast(dataType.getArrayDataType());
        })
        .orElseThrow(
            () -> illegalArgumentException("Field '%s' is not a list of enumerations.", objectField.getName()));
  }

  private Optional<Condition> createGeometryCondition(PostgresObjectField objectField, FilterOperator operator,
      Object value) {
    if (SRID == operator) {
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

  private String escapeMatchValue(String inputValue) {
    String result = inputValue.replace(String.valueOf(LIKE_ESCAPE_CHARACTER),
        String.valueOf(new char[] {LIKE_ESCAPE_CHARACTER, LIKE_ESCAPE_CHARACTER}));
    result = result.replace("_", LIKE_ESCAPE_CHARACTER + "_");
    result = result.replace("%", LIKE_ESCAPE_CHARACTER + "%");
    return result;
  }
}
