package org.dotwebstack.framework.backend.postgres.query;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.AggregateFieldHelper.isStringJoin;
import static org.dotwebstack.framework.backend.postgres.query.BatchJoinBuilder.newBatchJoining;
import static org.dotwebstack.framework.backend.postgres.query.BatchSingleJoinBuilder.newBatchSingleJoin;
import static org.dotwebstack.framework.backend.postgres.query.FilterConditionBuilder.newFiltering;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.PagingBuilder.newPaging;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.backend.postgres.query.SortBuilder.newSorting;
import static org.dotwebstack.framework.backend.postgres.query.SortHelper.addSortFields;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateObjectRequest;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
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

    return newBatchJoining().requestContext(requestContext)
        .aliasManager(aliasManager)
        .fieldMapper(fieldMapper)
        .dataQuery(dataQuery)
        .table(dataTable)
        .joinCriteria(joinCriteria)
        .objectRequest(collectionRequest.getObjectRequest())
        .build();
  }

  public SelectQuery<Record> build(BatchRequest batchRequest) {
    var dataQuery = build(batchRequest.getObjectRequest());

    return newBatchSingleJoin().aliasManager(aliasManager)
        .fieldMapper(fieldMapper)
        .dataQuery(dataQuery)
        .keys(batchRequest.getKeys())
        .build();
  }

  public SelectQuery<Record> build(ObjectRequest objectRequest) {
    return build(objectRequest, aliasManager.newAlias());
  }

  @SuppressWarnings("squid:S2637")
  public SelectQuery<Record> build(ObjectRequest objectRequest, String tableAlias) {
    validateFields(this);

    var objectType = getObjectType(objectRequest);

    var dataTable =
        ofNullable(objectType.getTable()).map(tableName -> findTable(tableName, objectRequest.getContextCriteria()))
            .map(table -> table.as(tableAlias))
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

    List<Condition> keyConditions = Optional.ofNullable(objectRequest.getKeyCriteria())
        .stream()
        .flatMap(keyCriteria -> createKeyCondition(keyCriteria, objectType, selectTable).stream())
        .collect(Collectors.toList());

    dataQuery.addConditions(keyConditions);

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
        .map(nestedSelectResult -> {
          if (nestedSelectResult.selectQuery != null) {
            addSubSelect(dataQuery, nestedSelectResult.selectQuery, objectRequest.getObjectType()
                .isNested());
          }
          return nestedSelectResult.selectFieldOrAsterisk;
        })
        .filter(Objects::nonNull)
        .forEach(dataQuery::addSelect);

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
        .tableCreator(createTableCreator(subSelect, contextCriteria, aliasManager))
        .build()
        .forEach(subSelect::addConditions);

    return subSelect;
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

  private Optional<Condition> createKeyCondition(KeyCriteria keyCriteria, PostgresObjectType objectType,
      Table<Record> table) {
    if (keyCriteria == null) {
      return Optional.empty();
    }

    return Optional.of(DSL.and(keyCriteria.getValues()
        .entrySet()
        .stream()
        .map(entry -> Optional.of(objectType.getField(entry.getKey()))
            .map(PostgresObjectField::getColumn)
            .map(column -> column(table, column).equal(entry.getValue()))
            .orElseThrow())
        .collect(Collectors.toList())));
  }

  private List<SelectFieldOrAsterisk> processScalarField(FieldRequest fieldRequest, PostgresObjectType objectType,
      Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper) {
    var objectField = objectType.getField(fieldRequest.getName());

    List<SelectFieldOrAsterisk> result = new ArrayList<>();

    if (StringUtils.isNotBlank(objectField.getKeyField())) {
      var keyFieldRequest = FieldRequest.builder()
          .name(objectField.getName())
          .build();

      result.addAll(processScalarField(keyFieldRequest, objectType, table, parentMapper));
    }

    String column;
    if (objectType.isNested() && scalarReferences.size() > 0) {
      column = ofNullable(scalarReferences.get(fieldRequest.getName()))
          .orElseThrow(() -> illegalStateException("Missing scalar reference for field '{}", fieldRequest.getName()));
    } else {
      column = objectField.getColumn();
    }

    ColumnMapper columnMapper;
    if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
      columnMapper = createSpatialColumnMapper(column, table, objectField, fieldRequest);
    } else {
      columnMapper = createColumnMapper(column, table);
    }

    parentMapper.register(fieldRequest.getName(), columnMapper);

    result.add(columnMapper.getColumn());

    return result;
  }

  private SpatialColumnMapper createSpatialColumnMapper(String columnName, Table<Record> table,
      PostgresObjectField objectField, FieldRequest fieldRequest) {
    String spatialColumnName = SpatialHelper.getColummName(columnName, objectField, fieldRequest);

    var column = column(table, spatialColumnName).as(aliasManager.newAlias());
    var requestedSrid = SpatialHelper.getRequestedSrid(fieldRequest);

    return new SpatialColumnMapper(column, objectField.getSpatialReferenceSystems(), requestedSrid);
  }

  private ColumnMapper createColumnMapper(String columnName, Table<Record> table) {
    var column = column(table, columnName).as(aliasManager.newAlias());

    return new ColumnMapper(column);
  }

  private Stream<ObjectListFieldResult> createNestedSelect(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> parentTable) {

    // get key field for batch-single
    if (objectField.getKeyField() != null) {
      var keyObjectField = (PostgresObjectField) objectField.getObjectType()
          .getField(objectField.getKeyField());
      var keyObjectRequest = ObjectRequest.builder()
          .objectType(keyObjectField.getTargetType())
          .scalarFields(keyObjectField.getTargetType()
              .getFields()
              .keySet()
              .stream()
              .map(fieldName -> FieldRequest.builder()
                  .name(fieldName)
                  .build())
              .collect(Collectors.toList()))
          .build();

      return createNestedSelect(keyObjectField, keyObjectRequest, parentTable);
    }

    // Relation object implementation
    if (objectField.getTargetType()
        .isNested() && objectField.getJoinTable() != null) {

      ObjectFieldMapper<Map<String, Object>> nestedFieldMapper = new ObjectMapper();

      fieldMapper.register(objectField.getName(), nestedFieldMapper);

      return objectRequest.getObjectListFields()
          .keySet()
          .stream()
          .flatMap(fieldRequest -> {
            var joinTable = objectField.getJoinTable();

            // Asked for reference
            if (joinTable.getInverseJoinColumns()
                .stream()
                .anyMatch(joinColumn -> joinColumn.getReferencedField()
                    .startsWith(fieldRequest.getName()))) {
              var objectType = (PostgresObjectType) objectField.getObjectType();

              var query = dslContext.selectQuery(findTable(joinTable.getName(), objectRequest.getContextCriteria()));

              Map<String, String> identifyingObjectMapping = createIdentifyingObjectMapping(joinTable, query);

              createJoinConditions(null, parentTable, joinTable.getJoinColumns(), objectType)
                  .forEach(query::addConditions);

              fieldMapper.register(objectField.getName(), new IdentifyingObjectMapper(identifyingObjectMapping));

              return Stream.of(ObjectListFieldResult.builder()
                  .selectQuery(query)
                  .build());
            }

            // Asked for joinTable
            nestedFieldMapper.register(JOIN_KEY_PREFIX.concat(fieldRequest.getName()), row -> JoinCondition.builder()
                .key(getJoinColumnValues(joinTable.getJoinColumns(), row))
                .build());

            return selectJoinColumns((PostgresObjectType) objectField.getObjectType(), joinTable.getJoinColumns(),
                parentTable).stream()
                    .map(selectFieldOrAsterisk -> ObjectListFieldResult.builder()
                        .selectFieldOrAsterisk(selectFieldOrAsterisk)
                        .build());
          });
    }

    var objectMapper = new ObjectMapper(aliasManager.newAlias());
    var objectType = (PostgresObjectType) objectRequest.getObjectType();

    fieldMapper.register(objectField.getName(), objectMapper);

    var tableName = aliasManager.newAlias();

    var select = newSelect().requestContext(requestContext)
        .fieldMapper(objectMapper)
        .aliasManager(aliasManager)
        .parentTable(parentTable)
        .scalarReferences(createScalarReferences(objectField))
        .build(objectRequest, tableName);

    select.addSelect(DSL.field("1")
        .as(objectMapper.getAlias()));

    if (!objectField.isList() && !objectType.isNested()) {
      select.addLimit(1);
    }

    if (!objectType.isNested() && !objectField.getObjectType()
        .isNested()) {
      newJoin().table(parentTable)
          .relatedTable(DSL.table(tableName))
          .current(objectField)
          .tableCreator(createTableCreator(select, objectRequest.getContextCriteria(), aliasManager))
          .build()
          .forEach(select::addConditions);
    }

    return Stream.of(ObjectListFieldResult.builder()
        .selectQuery(select)
        .build());
  }

  private Map<String, String> createIdentifyingObjectMapping(JoinTable joinTable, SelectQuery<Record> query) {
    return joinTable.getInverseJoinColumns()
        .stream()
        .collect(Collectors.toMap(JoinColumn::getReferencedField, joinColumn -> {
          var alias = aliasManager.newAlias();

          var field = DSL.field(joinColumn.getName());

          query.addSelect(DSL.arrayAgg(field)
              .as(alias));

          return alias;
        }));
  }

  private Map<String, String> createScalarReferences(PostgresObjectField objectField) {
    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return objectField.getJoinColumns()
          .stream()
          .collect(Collectors.toMap(JoinColumn::getReferencedField, JoinColumn::getName));
    } else if (objectField.getJoinTable() != null) {
      return scalarReferences;
    } else {
      return scalarReferences.entrySet()
          .stream()
          .filter(entry -> entry.getKey()
              .startsWith(String.format("%s.", objectField.getName())))
          .map(entry -> Map.entry(substringAfter(entry.getKey(), String.format("%s.", objectField.getName())),
              entry.getValue()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
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

    if (objectField.getJoinColumns() != null) {
      // Provide join info for child data fetcher
      fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
          .key(objectField.getJoinColumns()
              .stream()
              .collect(HashMap::new, (map, joinColumn) -> {
                var key = joinColumn.getName();
                map.put(key, fieldMapper.getFieldMapper(key)
                    .apply(row));
              }, HashMap::putAll))
          .build());

      return objectField.getJoinColumns()
          .stream()
          .map(JoinColumn::getName)
          .map(columnName -> getColumnMapper(table, columnName).getColumn())
          .map(field -> ObjectListFieldResult.builder()
              .selectFieldOrAsterisk(field)
              .build())
          .collect(Collectors.toList());
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

  private List<ObjectListFieldResult> handleJoinTable(PostgresObjectField objectField, Table<Record> parentTable) {
    var joinTable = objectField.getJoinTable();

    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
        .key(getJoinColumnValues(joinTable.getJoinColumns(), row))
        .build());

    return selectJoinColumns((PostgresObjectType) objectField.getObjectType(), joinTable.getJoinColumns(), parentTable)
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
