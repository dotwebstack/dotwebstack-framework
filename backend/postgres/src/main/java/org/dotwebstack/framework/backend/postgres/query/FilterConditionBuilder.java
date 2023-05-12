package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getRequestedSrid;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.JoinConfiguration.toJoinConfiguration;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.andCondition;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getFieldValue;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.EXISTS_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINS_ALL_OF;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINS_ANY_OF;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.EQ;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.EQ_IGNORE_CASE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.GT;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.GTE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.IN;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.IN_IGNORE_CASE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.LT;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.LTE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.MATCH;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.NOT;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;
import static org.jooq.impl.DefaultDataType.getDefaultDataType;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.filter.ObjectFieldFilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.util.postgres.PostgresDSL;

@Accessors(fluent = true)
@Setter
class FilterConditionBuilder {
  private static final String ERROR_MESSAGE = "Unknown filter field '%s' for type '%s'";

  private static final char LIKE_ESCAPE_CHARACTER = '\\';

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
          .toList();

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

    if (filterCriteria.isObjectFieldFilter()) {
      return walkFieldPath(filterCriteria.asObjectFieldFilter());
    }

    throw unsupportedOperationException("Filter criteria '{}' is not supported!", filterCriteria.getClass()
        .getSimpleName());
  }

  private Condition walkFieldPath(ObjectFieldFilterCriteria filterCriteria) {
    var fieldPath = filterCriteria.getFieldPath();
    var current = (PostgresObjectField) fieldPath.get(0);

    if (current.getTargetType() != null) {
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

      if (!childCriteria.getFieldPath()
          .isEmpty()) {
        var nestedCondition = newFiltering().aliasManager(aliasManager)
            .contextCriteria(contextCriteria)
            .table(filterTable)
            .filterCriteria(childCriteria)
            .build();

        filterQuery.addConditions(nestedCondition);
      } else if (Boolean.FALSE.equals(childCriteria.getValue()
          .get(EXISTS_FIELD))) {
        return DSL.notExists(filterQuery);
      }

      return DSL.exists(filterQuery);
    }

    return createCondition(current, filterCriteria);
  }

  private Condition createConditionsForMatchingNestedReference(ObjectFieldFilterCriteria filterCriteria,
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

  private List<Condition> createConditionsForMatchingNestedReference(ObjectFieldFilterCriteria filterCriteria,
      List<JoinColumn> joinColumns, String referencedField, String tableName) {
    return joinColumns.stream()
        .filter(joinColumn -> referencedField.equals(joinColumn.getReferencedField()))
        .map(joinColumn -> {
          var field = DSL.field(DSL.name(tableName, joinColumn.getName()));

          return createConditions(field, filterCriteria);
        })
        .toList();
  }

  private Condition createConditionsForMatchingNestedReference(ObjectFieldFilterCriteria filterCriteria,
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

  private ObjectFieldFilterCriteria createChildCriteria(FilterType filterType, List<ObjectField> fieldPath,
      Map<String, Object> value) {
    return ObjectFieldFilterCriteria.builder()
        .filterType(filterType)
        .fieldPath(fieldPath.subList(1, fieldPath.size()))
        .value(value)
        .build();
  }

  private Condition createConditions(Field<Object> field, ObjectFieldFilterCriteria filterCriteria) {
    var values = filterCriteria.getValue();
    var conditions = values.entrySet()
        .stream()
        .map(entry -> {
          var filterOperator = FilterOperator.getFilterOperator(entry.getKey(), filterCriteria.isCaseSensitive());
          return createCondition(null, field, filterOperator, entry.getValue());
        })
        .toList();

    return andCondition(conditions);
  }

  private Condition createCondition(PostgresObjectField objectField, ObjectFieldFilterCriteria filterCriteria) {
    var values = filterCriteria.getValue();
    var conditions = values.entrySet()
        .stream()
        .flatMap(entry -> Optional
            .ofNullable(FilterOperator.getFilterOperator(entry.getKey(), filterCriteria.isCaseSensitive()))
            .map(filterOperator -> {

              if (NOT == filterOperator) {
                return Stream.of(createNotCondition(objectField, filterCriteria, castToMap(entry.getValue())));
              }

              if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
                return createGeometryCondition(objectField, filterOperator, entry.getValue(), getRequestedSrid(values))
                    .stream();
              }

              return Stream.of(createCondition(objectField, filterOperator, entry.getValue()));
            })
            .orElseThrow(() -> illegalArgumentException(ERROR_MESSAGE, entry.getKey(), objectField.getType())))
        .toList();

    return andCondition(conditions);

  }

  private Condition createCondition(PostgresObjectField objectField, FilterOperator operator, Object value) {
    if (objectField.isList()) {
      var field = DSL.field(DSL.name(table.getName(), objectField.getColumn()), Object[].class);
      if (operator == MATCH) {
        return createConditionForList(objectField, field, operator, value);
      } else {
        var arrayValue = ObjectHelper.castToArray(value, objectField.getType());
        return createConditionForList(objectField, field, operator, arrayValue);
      }
    } else {
      var field = DSL.field(DSL.name(table.getName(), objectField.getColumn()));
      return createCondition(objectField, field, operator, value);
    }
  }

  private Condition createConditionForList(PostgresObjectField objectField, Field<Object[]> field,
      FilterOperator operator, Object value) {
    if (MATCH == operator) {
      return PostgresDSL.arrayToString(field, ",")
          .contains((String) value);
    }

    throw illegalArgumentException(ERROR_MESSAGE, operator, objectField.getType());
  }

  private Condition createConditionForList(PostgresObjectField objectField, Field<Object[]> field,
      FilterOperator operator, Object[] value) {

    if (EQ == operator) {
      return field.eq(getArrayValue(objectField, value));
    }

    if (CONTAINS_ALL_OF == operator) {
      return field.contains(getArrayValue(objectField, value));
    }

    if (CONTAINS_ANY_OF == operator) {
      return PostgresDSL.arrayOverlap(field, getArrayValue(objectField, value));
    }

    throw illegalArgumentException(ERROR_MESSAGE, operator, objectField.getType());
  }

  @SuppressWarnings("squid:S3776")
  private Condition createCondition(PostgresObjectField objectField, Field<Object> field, FilterOperator operator,
      Object value) {
    if (MATCH == operator) {
      var stringField = DSL.field(field.getQualifiedName(), String.class);

      var escapedValue = escapeMatchValue(Objects.toString(value));

      return DSL.lower(stringField)
          .like(DSL.lower(DSL.val(String.format("%%%s%%", escapedValue))))
          .escape(LIKE_ESCAPE_CHARACTER);
    }

    if (EQ == operator) {
      return value != null ? field.eq(getValue(objectField, value)) : field.isNull();
    }

    if (EQ_IGNORE_CASE == operator) {
      return value != null ? field.equalIgnoreCase((Field<String>) getValue(objectField, value)) : field.isNull();
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

    if (isInIgnoreCaseAndOfTypeString(operator, objectField)) {
      var lowerField = DSL.lower(DSL.field(DSL.name(table.getName(), objectField.getColumn()), String.class));
      var arrayValues = ObjectHelper.castToArray(value, String.class, true);
      return lowerField.in(arrayValues);
    }

    throw illegalArgumentException(ERROR_MESSAGE, operator, objectField.getType());
  }

  private boolean isInIgnoreCaseAndOfTypeString(FilterOperator operator, PostgresObjectField objectField) {
    return IN_IGNORE_CASE == operator && objectField.getType()
        .equals("String");
  }

  private Condition createNotCondition(PostgresObjectField objectField, ObjectFieldFilterCriteria filterCriteria,
      Map<String, Object> values) {

    var conditions = values.entrySet()
        .stream()
        .map(entry -> Optional
            .ofNullable(FilterOperator.getFilterOperator(entry.getKey(), filterCriteria.isCaseSensitive()))
            .map(filterOperator -> {
              if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
                return createGeometryCondition(objectField, filterOperator, entry.getValue(), getRequestedSrid(values))
                    .orElseThrow();
              }

              return createCondition(objectField, filterOperator, entry.getValue());
            })
            .orElseThrow(() -> illegalArgumentException(ERROR_MESSAGE, entry.getKey(), objectField.getType())))
        .toList();

    return DSL.not(andCondition(conditions));
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

    if (objectField == null) {
      return DSL.val(value);
    }

    return getFieldValue(objectField, value);
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
      Object value, Integer requestedSrid) {
    var geoConditionBuilder = GeometryConditionBuilderFactory.getGeometryConditionBuilder(objectField, operator);
    return geoConditionBuilder.sourceTable(table)
        .value(value)
        .srid(requestedSrid)
        .build();
  }

  private String escapeMatchValue(String inputValue) {
    String result = inputValue.replace(String.valueOf(LIKE_ESCAPE_CHARACTER),
        String.valueOf(new char[] {LIKE_ESCAPE_CHARACTER, LIKE_ESCAPE_CHARACTER}));
    result = result.replace("_", LIKE_ESCAPE_CHARACTER + "_");
    result = result.replace("%", LIKE_ESCAPE_CHARACTER + "%");
    return result;
  }
}
