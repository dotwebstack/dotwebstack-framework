package org.dotwebstack.framework.backend.postgres.query;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.AggregateFieldHelper.isStringJoin;
import static org.dotwebstack.framework.backend.postgres.query.FilterConditionBuilder.newFiltering;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.PagingBuilder.newPaging;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.backend.postgres.query.SortBuilder.newSorting;
import static org.dotwebstack.framework.backend.postgres.query.SortHelper.addSortFields;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateObjectRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Setter
@Accessors(fluent = true)
class SelectBuilder {

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  @NotNull
  private RequestContext requestContext;

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @NotNull
  private AliasManager aliasManager;

  private Table<Record> parentTable;

  private Map<String, String> scalarReferences = new HashMap<>();

  private SelectBuilder() {}

  public static SelectBuilder newSelect() {
    return new SelectBuilder();
  }

  public SelectQuery<Record> build(CollectionRequest collectionRequest, JoinCriteria joinCriteria) {
    validateFields(this);

    addSortFields(collectionRequest);

    var objectType = (PostgresObjectType) collectionRequest.getObjectRequest()
        .getObjectType();

    var dataTable = createTable(objectType, collectionRequest.getObjectRequest()
        .getContextCriteria());
    var dataQuery = createDataQuery(collectionRequest.getObjectRequest(), dataTable);

    newSorting().sortCriterias(collectionRequest.getSortCriterias())
        .fieldMapper(fieldMapper)
        .build()
        .forEach(dataQuery::addOrderBy);

    newFiltering().aliasManager(aliasManager)
        .filterCriterias(collectionRequest.getFilterCriterias())
        .table(dataTable)
        .objectRequest(collectionRequest.getObjectRequest())
        .build()
        .forEach(dataQuery::addConditions);

    newPaging().requestContext(requestContext)
        .dataQuery(dataQuery)
        .build();

    if (joinCriteria == null) {
      return dataQuery;
    }

    return BatchJoinBuilder.newBatchJoining()
        .requestContext(requestContext)
        .aliasManager(aliasManager)
        .fieldMapper(fieldMapper)
        .dataQuery(dataQuery)
        .table(dataTable)
        .joinCriteria(joinCriteria)
        .objectRequest(collectionRequest.getObjectRequest())
        .build();
  }

  @SuppressWarnings("squid:S2637")
  public SelectQuery<Record> build(ObjectRequest objectRequest) {
    validateFields(this);

    var objectType = getObjectType(objectRequest);

    var dataTable =
        ofNullable(objectType.getTable()).map(tableName -> findTable(tableName, objectRequest.getContextCriteria()))
            .map(table -> table.as(aliasManager.newAlias()))
            .orElse(null);

    return createDataQuery(objectRequest, dataTable);
  }

  private Table<Record> createTable(PostgresObjectType objectType, ContextCriteria contextCriteria) {
    return findTable(objectType.getTable(), contextCriteria).as(aliasManager.newAlias());
  }

  private SelectQuery<Record> createDataQuery(ObjectRequest objectRequest, Table<Record> table) {
    var objectType = getObjectType(objectRequest);
    var dataQuery = dslContext.selectQuery();

    Table<Record> selectTable = (table != null) ? table : parentTable;

    if (table != null) {
      dataQuery.addFrom(table);
    }

    objectRequest.getKeyCriteria()
        .stream()
        .map(keyCriteria -> createKeyConditions(keyCriteria, objectType, selectTable))
        .forEach(dataQuery::addConditions);

    objectRequest.getScalarFields()
        .stream()
        .map(selectedField -> processScalarField(selectedField, objectType, selectTable, fieldMapper))
        .forEach(dataQuery::addSelect);

    objectRequest.getAggregateObjectFields()
        .stream()
        .flatMap(aggregateObjectField -> processAggregateObjectField(objectRequest, aggregateObjectField, table))
        .collect(Collectors.toList())
        .forEach(nestedSelect -> addSubSelect(dataQuery, nestedSelect, objectRequest.getObjectType()
            .isNested()));

    objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .flatMap(entry -> createNestedSelect(getObjectField(objectRequest, entry.getKey()
            .getName()), entry.getValue(), selectTable))
        .forEach(nestedSelect -> addSubSelect(dataQuery, nestedSelect, objectRequest.getObjectType()
            .isNested()));

    objectRequest.getObjectListFields()
        .entrySet()
        .stream()
        .flatMap(entry -> {
          var objectField = getObjectField(objectRequest, entry.getKey()
              .getName());

          var collectionRequest = entry.getValue();

          return processObjectListFields(objectField, collectionRequest, table).stream()
              .map(objectListFieldResult -> {
                if (objectListFieldResult.selectQuery() != null) {
                  addSubSelect(dataQuery, objectListFieldResult.selectQuery(), false);
                }
                return objectListFieldResult.selectFieldOrAsterisk();
              });
        })
        .filter(Objects::nonNull)
        .forEach(dataQuery::addSelect);

    return dataQuery;
  }

  private void addSubSelect(SelectQuery<Record> selectQuery, Select<Record> nestedSelectQuery, boolean addFrom) {
    var nestedTable = nestedSelectQuery.asTable()
        .as(aliasManager.newAlias());

    selectQuery.addSelect(DSL.field(String.format("\"%s\".*", nestedTable.getName())));

    if (addFrom) {
      selectQuery.addFrom(nestedTable);
    } else {
      var lateralTable = DSL.lateral(nestedTable);
      selectQuery.addJoin(lateralTable, JoinType.LEFT_OUTER_JOIN);
    }
  }

  private Stream<Select<Record>> processAggregateObjectField(ObjectRequest objectRequest,
      AggregateObjectRequest aggregateObjectRequest, Table<Record> table) {
    var aggregateObjectMapper = new ObjectMapper();

    fieldMapper.register(aggregateObjectRequest.getObjectField()
        .getName(), aggregateObjectMapper);

    var objectField = (PostgresObjectField) aggregateObjectRequest.getObjectField();

    var stringJoinResult = aggregateObjectRequest.getAggregateFields()
        .stream()
        .filter(isStringJoin)
        .map(aggregateField -> processAggregateFields(objectField, List.of(aggregateField), aggregateObjectMapper,
            table, objectRequest.getContextCriteria()));

    var otherResult = Stream.of(processAggregateFields(objectField, aggregateObjectRequest.getAggregateFields()
        .stream()
        .filter(not(isStringJoin))
        .collect(Collectors.toList()), aggregateObjectMapper, table, objectRequest.getContextCriteria()));

    return Stream.concat(stringJoinResult, otherResult);
  }

  private SelectQuery<Record> processAggregateFields(PostgresObjectField objectField,
      List<AggregateField> aggregateFields, ObjectMapper aggregateObjectMapper, Table<Record> table,
      ContextCriteria contextCriteria) {
    var aggregateObjectType = (PostgresObjectType) objectField.getAggregationOfType();

    var aliasedAggregateTable =
        findTable(aggregateObjectType.getTable(), contextCriteria).asTable(aliasManager.newAlias());

    var subSelect = dslContext.selectQuery(aliasedAggregateTable);

    aggregateFields.forEach(aggregateField -> processAggregateField(aggregateField, aggregateObjectMapper, subSelect,
        aliasedAggregateTable));

    newJoin().table(table)
        .current(objectField)
        .tableCreator(createTableCreator(subSelect, contextCriteria))
        .build()
        .forEach(subSelect::addConditions);

    return subSelect;
  }

  private Function<String, Table<Record>> createTableCreator(SelectQuery<Record> query,
      ContextCriteria contextCriteria) {
    return tableName -> {
      var requestedTable = QueryHelper.findTable(tableName, contextCriteria);

      query.addFrom(requestedTable);
      return requestedTable;
    };
  }

  private void processAggregateField(AggregateField aggregateField, ObjectMapper aggregateMapper, SelectQuery<?> query,
      Table<?> table) {
    var columnAlias = aliasManager.newAlias();
    var columnName = ((PostgresObjectField) aggregateField.getField()).getColumn();

    var column = AggregateFieldHelper.create(aggregateField, table.getName(), columnName, columnAlias)
        .as(columnAlias);

    aggregateMapper.register(aggregateField.getAlias(), row -> row.get(columnAlias));

    query.addSelect(column);

    if (aggregateField.getFunctionType() == JOIN && aggregateField.getField()
        .isList()) {
      query.addJoin(DSL.unnest(DSL.field(DSL.name(table.getName(), columnName), String[].class))
          .as(columnAlias), JoinType.CROSS_JOIN);
    }
  }

  private List<Condition> createKeyConditions(KeyCriteria keyCriteria, PostgresObjectType objectType,
      Table<Record> table) {
    return keyCriteria.getValues()
        .entrySet()
        .stream()
        .map(entry -> Optional.of(objectType.getField(entry.getKey()))
            .map(PostgresObjectField::getColumn)
            .map(column -> column(table, column).equal(entry.getValue()))
            .orElseThrow())
        .collect(Collectors.toList());
  }

  private SelectFieldOrAsterisk processScalarField(FieldRequest fieldRequest, PostgresObjectType objectType,
      Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper) {
    var objectField = objectType.getField(fieldRequest.getName());

    String column;

    if (objectType.isNested() && scalarReferences.size() > 0) {
      column = ofNullable(scalarReferences.get(fieldRequest.getName()))
          .orElseThrow(() -> illegalStateException("Missing scalar reference for field '{}", fieldRequest.getName()));
    } else {
      column = objectField.getColumn();
    }

    var columnMapper = createColumnMapper(column, table);

    parentMapper.register(fieldRequest.getName(), columnMapper);

    return columnMapper.getColumn();
  }

  private ColumnMapper createColumnMapper(String columnName, Table<Record> table) {
    var column = column(table, columnName).as(aliasManager.newAlias());

    return new ColumnMapper(column);
  }


  private Stream<SelectQuery<Record>> createNestedSelect(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table) {
    var objectMapper = new ObjectMapper(aliasManager.newAlias());
    var objectType = objectRequest.getObjectType();

    fieldMapper.register(objectField.getName(), objectMapper);

    var select = SelectBuilder.newSelect()
        .requestContext(requestContext)
        .fieldMapper(objectMapper)
        .aliasManager(aliasManager)
        .parentTable(table)
        .scalarReferences(createScalarReferences(objectField))
        .build(objectRequest);

    select.addSelect(DSL.field("1")
        .as(objectMapper.getAlias()));

    if (!objectField.isList() && !objectType.isNested()) {
      select.addLimit(1);
    }

    if (!objectType.isNested()) {
      newJoin().table(table)
          .current(objectField)
          .tableCreator(createTableCreator(select, objectRequest.getContextCriteria()))
          .build()
          .forEach(select::addConditions);
    }

    return Stream.of(select);
  }

  private Map<String, String> createScalarReferences(PostgresObjectField objectField) {
    Map<String, String> nestedScalarReferences;
    if (!objectField.getJoinColumns()
        .isEmpty()) {
      nestedScalarReferences = objectField.getJoinColumns()
          .stream()
          .collect(Collectors.toMap(JoinColumn::getReferencedField, JoinColumn::getName));
    } else {
      nestedScalarReferences = scalarReferences.entrySet()
          .stream()
          .filter(entry -> entry.getKey()
              .startsWith(String.format("%s.", objectField.getName())))
          .map(entry -> Map.entry(substringAfter(entry.getKey(), String.format("%s.", objectField.getName())),
              entry.getValue()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    return nestedScalarReferences;
  }

  @Builder
  @Getter
  private static class ObjectListFieldResult {
    private SelectFieldOrAsterisk selectFieldOrAsterisk;

    private SelectQuery<Record> selectQuery;
  }

  private List<ObjectListFieldResult> processObjectListFields(PostgresObjectField objectField,
      CollectionRequest collectionRequest, Table<Record> table) {

    if (objectField.getJoinTable() != null) {
      return handleJoinTable(objectField, table);
    }

    if (objectField.getMappedBy() != null) {
      return handleJoinMappedBy(collectionRequest, objectField, table);
    }

    return List.of();
  }

  private List<ObjectListFieldResult> handleJoinMappedBy(CollectionRequest collectionRequest,
      PostgresObjectField objectField, Table<Record> table) {
    var nestedObjectField = getObjectField(collectionRequest.getObjectRequest(), objectField.getMappedBy());

    // Provide join info for child data fetcher
    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
        .key(getJoinColumnValues(nestedObjectField.getJoinColumns(), row))
        .build());

    return selectJoinColumns((PostgresObjectType) nestedObjectField.getTargetType(), nestedObjectField.getJoinColumns(),
        table).stream()
            .map(selectFieldOrAsterisk -> ObjectListFieldResult.builder()
                .selectFieldOrAsterisk(selectFieldOrAsterisk)
                .build())
            .collect(Collectors.toList());
  }

  private List<ObjectListFieldResult> handleJoinTable(PostgresObjectField objectField, Table<Record> table) {
    var joinTable = objectField.getJoinTable();

    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
        .key(getJoinColumnValues(joinTable.getJoinColumns(), row))
        .build());

    return selectJoinColumns((PostgresObjectType) objectField.getObjectType(), joinTable.getJoinColumns(), table)
        .stream()
        .map(selectFieldOrAsterisk -> ObjectListFieldResult.builder()
            .selectFieldOrAsterisk(selectFieldOrAsterisk)
            .build())
        .collect(Collectors.toList());
  }

  private Map<String, Object> getJoinColumnValues(List<JoinColumn> joinColumns, Map<String, Object> row) {
    return joinColumns.stream()
        .collect(HashMap::new, (map, joinColumn) -> {
          var key = (joinColumn.getReferencedField() != null ? joinColumn.getReferencedField()
              : joinColumn.getReferencedColumn());

          map.put(key, fieldMapper.getFieldMapper(key)
              .apply(row));
        }, HashMap::putAll);
  }

  private List<SelectFieldOrAsterisk> selectJoinColumns(PostgresObjectType objectType, List<JoinColumn> joinColumns,
      Table<Record> table) {
    return joinColumns.stream()
        .map(joinColumn -> {
          ColumnMapper columnMapper;
          if (joinColumn.getReferencedColumn() != null) {
            columnMapper = getColumnMapper(table, joinColumn.getReferencedColumn());
          } else {
            columnMapper = getColumnMapper(table, joinColumn.getReferencedField(), objectType);
          }
          return columnMapper.getColumn();
        })
        .collect(Collectors.toList());
  }

  private ColumnMapper getColumnMapper(Table<Record> table, String referencedField, PostgresObjectType objectType) {
    var objectField = objectType.getField(referencedField);

    ColumnMapper columnMapper = createColumnMapper(objectField.getColumn(), table);
    fieldMapper.register(objectField.getName(), columnMapper);
    return columnMapper;
  }

  private ColumnMapper getColumnMapper(Table<Record> table, String referencedColumn) {
    ColumnMapper columnMapper = createColumnMapper(referencedColumn, table);
    fieldMapper.register(referencedColumn, columnMapper);
    return columnMapper;
  }
}
