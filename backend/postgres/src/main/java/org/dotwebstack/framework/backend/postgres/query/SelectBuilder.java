package org.dotwebstack.framework.backend.postgres.query;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getRequestedSrid;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.isRequestedBbox;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.AggregateFieldHelper.isStringJoin;
import static org.dotwebstack.framework.backend.postgres.query.BatchJoinBuilder.newBatchJoining;
import static org.dotwebstack.framework.backend.postgres.query.FilterConditionBuilder.newFiltering;
import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.resolveJoinTable;
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
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
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
import org.dotwebstack.framework.core.query.model.JoinCondition;
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

    collectionRequest.getFilterCriterias()
        .stream()
        .map(filterCriteria -> newFiltering().aliasManager(aliasManager)
            .filterCriteria(filterCriteria)
            .table(DSL.table(tableAlias))
            .contextCriteria(collectionRequest.getObjectRequest()
                .getContextCriteria())
            .build())
        .forEach(dataQuery::addConditions);

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

    List<Condition> keyConditions = ofNullable(objectRequest.getKeyCriteria()).stream()
        .flatMap(keyCriteria -> createKeyCondition(keyCriteria, objectType, table).stream())
        .collect(Collectors.toList());

    dataQuery.addConditions(keyConditions);

    processScalarFields(objectRequest, objectType, dataQuery, table);
    processAggregateObjectFields(objectRequest, table, dataQuery);
    processObjectFields(objectRequest, objectType, dataQuery, table);
    processObjectListFields(objectRequest, table, dataQuery);

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
            .getName()), entry.getValue(), selectTable))
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
        .relatedTable(aliasedAggregateTable)
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
        .map(entry -> ofNullable(objectType.getField(entry.getKey())).map(PostgresObjectField::getColumn)
            .map(column -> column(table, column).equal(entry.getValue()))
            .orElseThrow())
        .collect(Collectors.toList())));
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

  private Stream<SelectResult> createNestedSelect(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table) {

    // Create a relation object
    if (JoinHelper.hasNestedReference(objectField)) {
      return createRelationObject(objectField, objectRequest, table).stream();
    }

    // Create a new nested object and take data from the same table
    if (objectField.getTargetType()
        .isNested()) {
      return createNestedObject(objectField, objectRequest, table);
    }

    // Create a new object and take data from another table and join with it
    return createObject(objectField, objectRequest, table);
  }

  private Stream<SelectResult> createObject(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table) {
    var objectMapper = new ObjectMapper(aliasManager.newAlias());
    fieldMapper.register(objectField.getName(), objectMapper);

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

    newJoin().table(table)
        .relatedTable(DSL.table(objectMapper.getAlias()))
        .current(objectField)
        .tableCreator(createTableCreator(select, objectRequest.getContextCriteria(), aliasManager))
        .build()
        .forEach(select::addConditions);

    return Stream.of(SelectResult.builder()
        .selectQuery(select)
        .build());
  }

  private List<SelectResult> createRelationObject(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table) {
    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return createRelationObject(objectField, objectField.getJoinColumns(), objectRequest, table);
    }

    var objectMapper = new ObjectMapper();
    fieldMapper.register(objectField.getName(), objectMapper);

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

          objectMapper.register(JOIN_KEY_PREFIX.concat(fieldRequest.getName()), row -> JoinCondition.builder()
              .key(getJoinColumnValues(joinTable.getJoinColumns(), row))
              .build());

          return selectJoinColumns((PostgresObjectType) objectField.getObjectType(), joinTable.getJoinColumns(), table)
              .stream()
              .map(field -> SelectResult.builder()
                  .selectFieldOrAsterisk(field)
                  .build());
        })
        .collect(Collectors.toList());
  }

  private List<SelectResult> createRelationObject(PostgresObjectField objectField, List<JoinColumn> joinColumns,
      ObjectRequest objectRequest, Table<Record> table) {
    var objectMapper = new ObjectMapper(aliasManager.newAlias());
    fieldMapper.register(objectField.getName(), objectMapper);

    var joinColumn = joinColumns.stream()
        .findFirst();

    var keyColumn = joinColumn.map(JoinColumn::getName)
        .orElseThrow();

    var keyTableField = DSL.field(DSL.name(table.getName(), keyColumn));

    var object = objectRequest.getObjectFields()
        .keySet()
        .stream()
        .flatMap(fieldRequest -> processReferenceObject(objectField, objectRequest, table, objectMapper, fieldRequest));

    return Stream.concat(Stream.of(keyTableField.as(objectMapper.getAlias())), object)
        .map(field -> SelectResult.builder()
            .selectFieldOrAsterisk(field)
            .build())
        .collect(Collectors.toList());
  }

  private boolean askedForReference(PostgresObjectField objectField, FieldRequest fieldRequest) {
    return objectField.getJoinTable()
        .getInverseJoinColumns()
        .stream()
        .anyMatch(joinColumn -> joinColumn.getReferencedField()
            .startsWith(fieldRequest.getName()));
  }

  private Stream<Field<Object>> processReferenceObject(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> table, ObjectMapper objectMapper, FieldRequest fieldRequest) {
    var objectType = getObjectType(objectRequest);
    var keyFieldName = objectType.getField(fieldRequest.getName())
        .getKeyField();

    String refFieldName;
    if (StringUtils.isNotBlank(keyFieldName)) {
      refFieldName = keyFieldName;
    } else {
      refFieldName = fieldRequest.getName();
    }

    var refFieldRequest = FieldRequest.builder()
        .name(refFieldName)
        .build();

    var refObjectField = objectType.getField(refFieldName);

    var refObjectRequest = createObjectRequest(refObjectField);

    return createReferenceObject(objectField, refObjectRequest, table, objectMapper, refFieldRequest);
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
      Table<Record> table) {
    var objectMapper = new ObjectMapper();
    fieldMapper.register(objectField.getName(), objectMapper);

    var result = objectRequest.getScalarFields()
        .stream()
        .map(scalarFieldRequest -> processScalarField(scalarFieldRequest, getObjectType(objectRequest), table,
            objectMapper))
        .map(columnMapper -> SelectResult.builder()
            .selectFieldOrAsterisk(columnMapper)
            .build())
        .collect(Collectors.toList());

    return result.stream();
  }

  private ObjectRequest createObjectRequest(PostgresObjectField keyObjectField) {
    return ObjectRequest.builder()
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
  }

  private SelectQuery<Record> getJoinTableReferences(PostgresObjectField objectField, ObjectRequest objectRequest,
      Table<Record> parentTable, ObjectFieldMapper<Map<String, Object>> nestedFieldMapper, FieldRequest fieldRequest) {
    var objectType = (PostgresObjectType) objectField.getObjectType();
    var joinTable = resolveJoinTable(objectType, objectField.getJoinTable());

    var table = findTable(joinTable.getName(), objectRequest.getContextCriteria()).as(aliasManager.newAlias());

    var query = dslContext.selectQuery(table);

    createJoinConditions(table, parentTable, joinTable.getJoinColumns(), objectType).forEach(query::addConditions);

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
        .collect(Collectors.toList());
  }

  private List<SelectFieldOrAsterisk> handleJoinColumn(PostgresObjectField objectField, List<JoinColumn> joinColumns,
      Table<Record> table) {
    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
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
    return newBatchJoining().objectField((PostgresObjectField) requestContext.getObjectField())
        .targetObjectType(getObjectType(objectRequest))
        .contextCriteria(objectRequest.getContextCriteria())
        .aliasManager(aliasManager)
        .fieldMapper(fieldMapper)
        .dataQuery(dataQuery)
        .table(dataTable)
        .joinCriteria(joinCriteria)
        .build();
  }
}
