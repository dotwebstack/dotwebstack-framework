package org.dotwebstack.framework.backend.postgres.query;

import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getRequestedSrid;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.isRequestedBbox;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.AggregateFieldHelper.isStringJoin;
import static org.dotwebstack.framework.backend.postgres.query.BatchJoinBuilder.newBatchJoining;
import static org.dotwebstack.framework.backend.postgres.query.FilterConditionBuilder.newFiltering;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.getExistFieldForRelationObject;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.resolveJoinColumns;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.resolveJoinTable;
import static org.dotwebstack.framework.backend.postgres.query.PagingBuilder.newPaging;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.backend.postgres.query.SortBuilder.newSorting;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getLeaf;
import static org.dotwebstack.framework.core.helpers.ObjectRequestHelper.addKeyFields;
import static org.dotwebstack.framework.core.helpers.ObjectRequestHelper.addSortFields;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
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
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
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

  @NotNull
  private String tableAlias;

  private SelectBuilder() {}

  public static SelectBuilder newSelect() {
    return new SelectBuilder();
  }

  public SelectQuery<Record> build(CollectionRequest collectionRequest, JoinCriteria joinCriteria) {
    validateFields(this);

    addSortFields(collectionRequest);

    var dataQuery = createDataQuery(collectionRequest.getObjectRequest());

    newSorting().sortCriterias(collectionRequest.getSortCriterias())
        .fieldMapper(fieldMapper)
        .build()
        .forEach(dataQuery::addOrderBy);

    Optional.of(collectionRequest)
        .map(CollectionRequest::getFilterCriteria)
        .map(filterCriteria -> newFiltering().aliasManager(aliasManager)
            .filterCriteria(filterCriteria)
            .table(DSL.table(tableAlias))
            .contextCriteria(collectionRequest.getObjectRequest()
                .getContextCriteria())
            .build())
        .ifPresent(dataQuery::addConditions);

    newPaging().requestContext(requestContext)
        .dataQuery(dataQuery)
        .build();

    if (joinCriteria == null) {
      return dataQuery;
    }

    return doBatchJoin(collectionRequest.getObjectRequest(), dataQuery, DSL.table(tableAlias), joinCriteria);
  }

  public SelectQuery<Record> build(BatchRequest batchRequest) {
    var dataQuery = build(batchRequest.getObjectRequest());

    var joinCriteria = JoinCriteria.builder()
        .keys(batchRequest.getKeys())
        .build();

    return doBatchJoin(batchRequest.getObjectRequest(), dataQuery, null, joinCriteria);
  }

  public SelectQuery<Record> build(ObjectRequest objectRequest) {
    return createDataQuery(objectRequest);
  }

  private SelectQuery<Record> createDataQuery(ObjectRequest objectRequest) {
    var objectType = getObjectType(objectRequest);

    var table = findTable(objectType.getTable(), objectRequest.getContextCriteria()).as(tableAlias);

    var dataQuery = dslContext.selectQuery(table);

    if (!objectRequest.getKeyCriterias()
        .isEmpty()) {
      addKeyFields(objectRequest);
    }

    processScalarFields(objectRequest, objectType, dataQuery, table);
    processAggregateObjectFields(objectRequest, table, dataQuery);
    processObjectFields(objectRequest, objectType, dataQuery, table);
    processObjectListFields(objectRequest, table, dataQuery);

    List<Condition> keyConditions = objectRequest.getKeyCriterias()
        .stream()
        .flatMap(keyCriteria -> createKeyCondition(keyCriteria, table).stream())
        .collect(Collectors.toList());

    dataQuery.addConditions(keyConditions);

    if (objectType.isDistinct()) {
      dataQuery.setDistinct(true);
    }

    return dataQuery;
  }

  private List<SelectFieldOrAsterisk> processObjectListFields(PostgresObjectField objectField, Table<Record> table) {

    if (objectField.getMappedByObjectField() != null) {
      var mappedByObjectField = objectField.getMappedByObjectField();

      if (mappedByObjectField.getJoinTable() != null) {
        return handleJoinColumn(objectField, mappedByObjectField.getJoinTable()
            .getInverseJoinColumns(), table);
      } else if (mappedByObjectField.getJoinColumns() != null) {
        return handleJoinColumn(objectField, mappedByObjectField.getJoinColumns(), table);
      }
    }

    if (objectField.getJoinTable() != null) {
      return handleJoinColumn(objectField, objectField.getJoinTable()
          .getJoinColumns(), table);
    }

    if (objectField.getJoinColumns() != null) {
      return handleJoinColumnSource(objectField, table);
    }

    return List.of();
  }

  private void processObjectListFields(ObjectRequest objectRequest, Table<Record> table,
      SelectQuery<Record> dataQuery) {
    objectRequest.getObjectListFields()
        .entrySet()
        .stream()
        .flatMap(entry -> {
          var objectField = getObjectField(objectRequest, entry.getKey()
              .getName());

          return processObjectListFields(objectField, table).stream();
        })
        .filter(Objects::nonNull)
        .forEach(dataQuery::addSelect);
  }

  private void processObjectFields(ObjectRequest objectRequest, PostgresObjectType objectType,
      SelectQuery<Record> dataQuery, Table<Record> selectTable) {
    objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .flatMap(entry -> createNestedSelect(getObjectField(objectRequest, entry.getKey()
            .getName()), entry.getKey()
                .getResultKey(),
            entry.getValue(), selectTable, fieldMapper))
        .map(selectResult -> {
          Optional.of(selectResult)
              .map(SelectResult::getSelectQuery)
              .ifPresent(selectQuery -> addSubSelect(dataQuery, selectQuery, objectType.isNested()));

          return selectResult.getSelectFieldOrAsterisk();
        })
        .filter(Objects::nonNull)
        .forEach(dataQuery::addSelect);
  }

  private void processAggregateObjectFields(ObjectRequest objectRequest, Table<Record> table,
      SelectQuery<Record> dataQuery) {
    objectRequest.getAggregateObjectFields()
        .stream()
        .flatMap(aggregateObjectField -> processAggregateObjectField(objectRequest, aggregateObjectField, table))
        .collect(Collectors.toList())
        .forEach(nestedSelect -> addSubSelect(dataQuery, nestedSelect, objectRequest.getObjectType()
            .isNested()));
  }

  private void processScalarFields(ObjectRequest objectRequest, PostgresObjectType objectType,
      SelectQuery<Record> dataQuery, Table<Record> selectTable) {
    objectRequest.getScalarFields()
        .stream()
        .map(scalarFieldRequest -> processScalarField(scalarFieldRequest, objectType, selectTable, fieldMapper))
        .forEach(dataQuery::addSelect);
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

    fieldMapper.register(aggregateObjectRequest.getKey(), aggregateObjectMapper);

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
    var aggregateObjectType = (PostgresObjectType) objectField.getTargetType();

    var aliasedAggregateTable =
        findTable(aggregateObjectType.getTable(), contextCriteria).asTable(aliasManager.newAlias());

    var subSelect = dslContext.selectQuery(aliasedAggregateTable);

    aggregateFields.forEach(aggregateField -> processAggregateField(aggregateField, aggregateObjectMapper, subSelect,
        aliasedAggregateTable));

    var joinConditions = newJoin().table(table)
        .joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .relatedTable(aliasedAggregateTable)
        .tableCreator(createTableCreator(subSelect, contextCriteria, aliasManager))
        .build();

    subSelect.addConditions(joinConditions);

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

  private Optional<Condition> createKeyCondition(KeyCriteria keyCriteria, Table<Record> table) {
    if (keyCriteria == null) {
      return Optional.empty();
    }

    var fieldPath = keyCriteria.getFieldPath();
    Field<Object> sqlField;
    if (fieldPath.size() == 2 && !getLeaf(fieldPath).getObjectType()
        .isNested()) {
      var leafFieldMapper = fieldMapper.getLeafFieldMapper(fieldPath);

      sqlField = column(null, leafFieldMapper.getAlias());
    } else {
      sqlField = column(table, ((PostgresObjectField) getLeaf(fieldPath)).getColumn());
    }

    var condition = sqlField.equal(keyCriteria.getValue());

    return Optional.of(JoinHelper.andCondition(List.of(condition)));
  }

  private SelectFieldOrAsterisk processScalarField(FieldRequest fieldRequest, PostgresObjectType objectType,
      Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper) {
    var objectField = objectType.getField(fieldRequest.getName());

    ColumnMapper columnMapper;
    if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
      columnMapper = createSpatialColumnMapper(table, objectField, fieldRequest);
    } else {
      columnMapper = createColumnMapper(objectField.getColumn(), table);
    }

    parentMapper.register(fieldRequest.getName(), columnMapper);

    return columnMapper.getColumn();
  }

  private SpatialColumnMapper createSpatialColumnMapper(Table<Record> table, PostgresObjectField objectField,
      FieldRequest fieldRequest) {
    var requestedSrid = getRequestedSrid(fieldRequest);
    var isRequestedBbox = isRequestedBbox(fieldRequest);

    var spatialColumnName =
        PostgresSpatialHelper.getColumnName(objectField.getSpatial(), requestedSrid, isRequestedBbox);
    var column = column(table, spatialColumnName).as(aliasManager.newAlias());

    return new SpatialColumnMapper(column, objectField.getSpatial(), requestedSrid, isRequestedBbox);
  }

  private ColumnMapper createColumnMapper(String columnName, Table<Record> table) {
    var column = column(table, columnName).as(aliasManager.newAlias());

    return new ColumnMapper(column);
  }

  private Stream<SelectResult> createNestedSelect(PostgresObjectField objectField, String key,
      ObjectRequest objectRequest, Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper) {

    // Create a relation object
    if (JoinHelper.hasNestedReference(objectField)) {
      return createRelationObject(objectField, objectRequest, table, parentMapper, key).stream();
    }

    // Create a new nested object and take data from the same table
    if (objectField.getTargetType()
        .isNested()) {
      return createNestedObject(objectField, objectRequest, table, parentMapper, key);
    }

    // Create a new object and take data from another table and join with it
    return createObject(objectField, objectRequest, table, parentMapper,
        JoinConfiguration.toJoinConfiguration(objectField), key);
  }

  private Stream<SelectResult> createObject(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper, JoinConfiguration joinConfiguration,
      String key) {
    var objectMapper = new ObjectMapper(aliasManager.newAlias());
    parentMapper.register(key, objectMapper);

    var select = newSelect().requestContext(requestContext)
        .fieldMapper(objectMapper)
        .aliasManager(aliasManager)
        .tableAlias(objectMapper.getAlias())
        .build(objectRequest);

    select.addSelect(DSL.field("1")
        .as(objectMapper.getAlias()));

    if (!objectField.isList()) {
      select.addLimit(1);
    }

    var joinConditions = newJoin().table(table)
        .relatedTable(DSL.table(objectMapper.getAlias()))
        .joinConfiguration(joinConfiguration)
        .tableCreator(createTableCreator(select, objectRequest.getContextCriteria(), aliasManager))
        .build();

    select.addConditions(joinConditions);

    return Stream.of(SelectResult.builder()
        .selectQuery(select)
        .build());
  }

  private List<SelectResult> createRelationObject(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper, String key) {
    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return createRelationObject(objectField, objectField.getJoinColumns(), objectRequest, table, parentMapper, key);
    }

    var objectMapper = new ObjectMapper();
    parentMapper.register(objectField.getName(), objectMapper);

    return objectRequest.getObjectListFields()
        .keySet()
        .stream()
        .flatMap(fieldRequest -> {
          // Asked for reference
          if (askedForReference(objectField, fieldRequest)) {
            return Stream.of(getJoinTableReferences(objectField, objectRequest, table, objectMapper, fieldRequest))
                .map(selectQuery -> SelectResult.builder()
                    .selectQuery(selectQuery)
                    .build());
          }

          // Asked for joinTable
          var joinTable = objectField.getJoinTable();

          objectMapper.register(JOIN_KEY_PREFIX.concat(fieldRequest.getName()), row -> {
            var resolvedJoinTable = resolveJoinTable((PostgresObjectType) objectField.getObjectType(), joinTable);

            return PostgresJoinCondition.builder()
                .key(getJoinColumnValues(joinTable.getJoinColumns(), row))
                .joinTable(resolvedJoinTable)
                .build();
          });

          return selectJoinColumns((PostgresObjectType) objectField.getObjectType(), joinTable.getJoinColumns(), table)
              .stream()
              .map(field -> SelectResult.builder()
                  .selectFieldOrAsterisk(field)
                  .build());
        })
        .collect(Collectors.toList());
  }

  private List<SelectResult> createRelationObject(PostgresObjectField objectField, List<JoinColumn> joinColumns,
      ObjectRequest objectRequest, Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper,
      String key) {
    var objectMapper = new ObjectMapper(aliasManager.newAlias());
    parentMapper.register(key, objectMapper);

    List<SelectResult> selectResults = new ArrayList<>();

    Field<Object> existField = getExistFieldForRelationObject(joinColumns, table, objectMapper.getAlias());

    selectResults.add(SelectResult.builder()
        .selectFieldOrAsterisk(existField)
        .build());

    objectRequest.getObjectFields()
        .keySet()
        .stream()
        .flatMap(fieldRequest -> processRelationObjectField(objectField, joinColumns, objectRequest, table,
            objectMapper, fieldRequest).stream())
        .forEach(selectResults::add);

    return selectResults;
  }

  private List<SelectResult> processRelationObjectField(PostgresObjectField objectField, List<JoinColumn> joinColumns,
      ObjectRequest objectRequest, Table<Record> table, ObjectMapper objectMapper, FieldRequest fieldRequest) {
    var childObjectRequest = objectRequest.getObjectFields()
        .get(fieldRequest);

    if (joinColumns.stream()
        .map(JoinColumn::getReferencedField)
        .filter(Objects::nonNull)
        .anyMatch(referencedField -> referencedField.startsWith(fieldRequest.getName()))) {
      return createReferenceObject(objectField, childObjectRequest, table, objectMapper, fieldRequest)
          .map(selectField -> SelectResult.builder()
              .selectFieldOrAsterisk(selectField)
              .build())
          .collect(Collectors.toList());
    } else {
      var childObjectField = getObjectType(objectRequest).getField(fieldRequest.getName());

      var joinConfiguration = JoinConfiguration.builder()
          .objectField(childObjectField)
          .targetType((PostgresObjectType) childObjectField.getTargetType())
          .objectType((PostgresObjectType) childObjectField.getObjectType())
          .mappedBy(objectField.getMappedByObjectField())
          .joinTable(resolveJoinTable((PostgresObjectType) objectField.getObjectType(), objectField.getJoinTable()))
          .joinColumns(resolveJoinColumns(objectField.getJoinColumns()))
          .build();

      return createObject(childObjectField, childObjectRequest, table, objectMapper, joinConfiguration,
          childObjectField.getName()).collect(Collectors.toList());
    }
  }

  private boolean askedForReference(PostgresObjectField objectField, FieldRequest fieldRequest) {
    return objectField.getJoinTable()
        .getInverseJoinColumns()
        .stream()
        .anyMatch(joinColumn -> joinColumn.getReferencedField()
            .startsWith(fieldRequest.getName()));
  }

  private Stream<Field<Object>> createReferenceObject(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table, ObjectMapper parentMapper, FieldRequest fieldRequest) {
    var objectMapper = new ObjectMapper(parentMapper.getAlias());
    parentMapper.register(fieldRequest.getName(), objectMapper);

    return objectRequest.getScalarFields()
        .stream()
        .flatMap(scalarFieldRequest -> objectField.getJoinColumns()
            .stream()
            .filter(joinColumn -> String.format("%s.%s", fieldRequest.getName(), scalarFieldRequest.getName())
                .equals(joinColumn.getReferencedField()))
            .map(joinColumn -> {
              var columnMapper = createColumnMapper(joinColumn.getName(), table);

              objectMapper.register(scalarFieldRequest.getName(), columnMapper);

              return columnMapper.getColumn();
            }));
  }

  private Stream<SelectResult> createNestedObject(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table, ObjectFieldMapper<Map<String, Object>> parentMapper, String key) {
    var presenceAlias = objectField.getPresenceColumn() == null ? null : aliasManager.newAlias();
    var objectMapper = new ObjectMapper(null, presenceAlias);

    parentMapper.register(key, objectMapper);

    List<SelectResult> selectResults = new ArrayList<>();

    if (objectField.getPresenceColumn() != null) {
      SelectResult presenceColumnSelect = createPresenceColumnSelect(objectField, table, objectMapper);
      selectResults.add(presenceColumnSelect);
    }

    objectRequest.getScalarFields()
        .stream()
        .map(scalarFieldRequest -> processScalarField(scalarFieldRequest,
            (PostgresObjectType) objectField.getTargetType(), table, objectMapper))
        .map(columnMapper -> SelectResult.builder()
            .selectFieldOrAsterisk(columnMapper)
            .build())
        .forEach(selectResults::add);

    objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .flatMap(entry -> createNestedSelect(getObjectField(objectRequest, entry.getKey()
            .getName()), entry.getKey()
                .getResultKey(),
            entry.getValue(), table, objectMapper))
        .forEach(selectResults::add);

    return selectResults.stream();
  }

  private SelectResult createPresenceColumnSelect(PostgresObjectField objectField, Table<Record> table,
      ObjectMapper objectMapper) {
    var column = column(table, objectField.getPresenceColumn());
    var columnIsNotNull = DSL.field(column.isNotNull())
        .as(objectMapper.getPresenceAlias());

    return SelectResult.builder()
        .selectFieldOrAsterisk(columnIsNotNull)
        .build();
  }

  private SelectQuery<Record> getJoinTableReferences(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> parentTable, ObjectFieldMapper<Map<String, Object>> nestedFieldMapper, FieldRequest fieldRequest) {
    var objectType = (PostgresObjectType) objectField.getObjectType();
    var joinTable = resolveJoinTable(objectType, objectField.getJoinTable());

    var table = findTable(joinTable.getName(), objectRequest.getContextCriteria()).as(aliasManager.newAlias());

    var query = dslContext.selectQuery(table);

    var joinConditions = createJoinConditions(table, parentTable, joinTable.getJoinColumns(), objectType);

    query.addConditions(joinConditions);

    var arrayObjectMapper = new ArrayObjectMapper();
    nestedFieldMapper.register(fieldRequest.getName(), arrayObjectMapper);

    joinTable.getInverseJoinColumns()
        .forEach(joinColumn -> {
          String alias = aliasManager.newAlias();

          var field = DSL.field(joinColumn.getName());

          var arrayAgg = DSL.arrayAgg(field)
              .as(alias);

          query.addSelect(arrayAgg);

          arrayObjectMapper.register(joinColumn.getReferencedField(), new ColumnMapper(field.as(alias)));
        });

    return query;
  }

  private List<SelectFieldOrAsterisk> handleJoinColumnSource(PostgresObjectField objectField, Table<Record> table) {
    var selectFields = objectField.getJoinColumns()
        .stream()
        .collect(Collectors.toMap(Function.identity(),
            joinColumn -> column(table, joinColumn.getName()).as(aliasManager.newAlias())));

    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> PostgresJoinCondition.builder()
        .key(selectFields.entrySet()
            .stream()
            .collect(HashMap::new, (map, joinSelectField) -> {
              var key = joinSelectField.getKey()
                  .getName();
              var value = row.get(joinSelectField.getValue()
                  .getName());

              if (value != null) {
                map.put(key, value);
              }
            }, HashMap::putAll))
        .build());

    return new ArrayList<>(selectFields.values());
  }

  private List<SelectFieldOrAsterisk> handleJoinColumn(PostgresObjectField objectField, List<JoinColumn> joinColumns,
      Table<Record> table) {
    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> PostgresJoinCondition.builder()
        .key(getJoinColumnValues(joinColumns, row))
        .build());

    return selectJoinColumns((PostgresObjectType) objectField.getObjectType(), joinColumns, table);
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

  private SelectQuery<Record> doBatchJoin(ObjectRequest objectRequest, SelectQuery<Record> dataQuery,
      Table<Record> dataTable, JoinCriteria joinCriteria) {
    var objectField = (PostgresObjectField) requestContext.getObjectField();

    var joinCondition = (PostgresJoinCondition) joinCriteria.getJoinCondition();

    return newBatchJoining().joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField, joinCondition))
        .contextCriteria(objectRequest.getContextCriteria())
        .aliasManager(aliasManager)
        .fieldMapper(fieldMapper)
        .dataQuery(dataQuery)
        .table(dataTable)
        .joinKeys(joinCriteria.getKeys())
        .build();
  }
}
