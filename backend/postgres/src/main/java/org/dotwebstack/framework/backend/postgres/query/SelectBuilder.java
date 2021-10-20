package org.dotwebstack.framework.backend.postgres.query;

import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.backend.postgres.query.AggregateFieldHelper.isStringJoin;
import static org.dotwebstack.framework.backend.postgres.query.Query.EXISTS_KEY;
import static org.dotwebstack.framework.backend.postgres.query.Query.GROUP_KEY;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.columnName;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.backend.BackendConstants.PAGING_KEY_PREFIX;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.model.ObjectField;
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
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

@Setter(onMethod = @__({@NonNull}))
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SelectBuilder {

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  private static final DataType<Geometry> GEOMETRY_DATATYPE =
      new DefaultDataType<>(SQLDialect.POSTGRES, Geometry.class, "geometry");

  private RequestContext requestContext;

  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private AliasManager aliasManager;

  public static SelectBuilder newSelect() {
    return new SelectBuilder();
  }

  public SelectQuery<Record> build(CollectionRequest collectionRequest, JoinCriteria joinCriteria) {
    addSortFields(collectionRequest);

    var dataTable = createTable(collectionRequest.getObjectRequest());
    var dataQuery = createDataQuery(collectionRequest.getObjectRequest(), dataTable);

    createSortConditions(collectionRequest.getSortCriterias()).forEach(dataQuery::addOrderBy);

    collectionRequest.getFilterCriterias()
        .stream()
        .map(filterCriteria -> createFilterCondition(collectionRequest.getObjectRequest(), filterCriteria.getFieldPath()
            .stream()
            .map(PostgresObjectField.class::cast)
            .collect(Collectors.toList()), filterCriteria.getValue(), dataTable))
        .forEach(dataQuery::addConditions);

    addPagingCriteria(dataQuery);

    if (joinCriteria == null) {
      return dataQuery;
    }

    var objectField = (PostgresObjectField) requestContext.getObjectField();

    if (objectField.getMappedByObjectField() != null) {
      objectField = objectField.getMappedByObjectField();
    }

    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return batchJoin(dataQuery, dataTable, joinCriteria, objectField.getJoinColumns());
    }

    if (objectField.getJoinTable() != null) {
      var targetObjectType = getObjectType(collectionRequest.getObjectRequest());
      return batchJoin(dataQuery, dataTable, joinCriteria, targetObjectType);
    }

    throw new UnsupportedOperationException();
  }

  public SelectQuery<Record> build(ObjectRequest objectRequest) {
    var objectType = getObjectType(objectRequest);
    var dataTable = findTable(objectType.getTable(), objectRequest.getContextCriteria()).as(aliasManager.newAlias());

    return createDataQuery(objectRequest, dataTable);
  }


  private void addSortFields(CollectionRequest collectionRequest) {
    collectionRequest.getSortCriterias()
        .forEach(sortCriteria -> addSortFields(collectionRequest, sortCriteria));
  }

  private void addSortFields(CollectionRequest collectionRequest, SortCriteria sortCriteria) {
    ObjectRequest objectRequest = collectionRequest.getObjectRequest();

    for (int index = 0; index < sortCriteria.getFields()
        .size(); index++) {
      ObjectField sortField = sortCriteria.getFields()
          .get(index);

      if (index == (sortCriteria.getFields()
          .size() - 1)) {
        findOrAddScalarField(objectRequest, sortField);
      } else {
        ObjectField nextSortField = sortCriteria.getFields()
            .get(index + 1);
        objectRequest = findOrAddObjectRequest(objectRequest.getObjectFields(), sortField, nextSortField);
      }
    }
  }

  private ObjectRequest findOrAddObjectRequest(Map<FieldRequest, ObjectRequest> objectFields, ObjectField objectField,
      ObjectField nextObjectField) {
    return objectFields.entrySet()
        .stream()
        .filter(field -> field.getKey()
            .getName()
            .equals(objectField.getName()))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseGet(() -> createObjectRequest(objectFields, objectField, nextObjectField));
  }

  private ObjectRequest createObjectRequest(Map<FieldRequest, ObjectRequest> objectFields, ObjectField objectField,
      ObjectField nextObjectField) {
    ObjectRequest objectRequest = ObjectRequest.builder()
        .objectType(nextObjectField.getObjectType())
        .build();
    FieldRequest field = FieldRequest.builder()
        .name(objectField.getName())
        .build();
    objectFields.put(field, objectRequest);
    return objectRequest;
  }

  private void findOrAddScalarField(ObjectRequest objectRequest, ObjectField objectField) {
    Optional<FieldRequest> scalarField = objectRequest.getScalarFields()
        .stream()
        .filter(field -> field.getName()
            .equals(objectField.getName()))
        .findFirst();

    if (scalarField.isEmpty()) {
      FieldRequest field = FieldRequest.builder()
          .name(objectField.getName())
          .build();
      objectRequest.getScalarFields()
          .add(field);
    }
  }

  private Table<Record> createTable(ObjectRequest objectRequest) {
    return findTable(getObjectType(objectRequest).getTable(), objectRequest.getContextCriteria())
        .as(aliasManager.newAlias());
  }

  private SelectQuery<Record> createDataQuery(ObjectRequest objectRequest, Table<Record> table) {
    var objectType = getObjectType(objectRequest);
    var dataQuery = dslContext.selectQuery(table);

    objectRequest.getKeyCriteria()
        .stream()
        .map(keyCriteria -> createKeyConditions(keyCriteria, objectType, table))
        .forEach(dataQuery::addConditions);

    objectRequest.getScalarFields()
        .stream()
        .map(selectedField -> processScalarField(selectedField, objectType, table, fieldMapper))
        .forEach(dataQuery::addSelect);

    objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .flatMap(entry -> createNestedSelect(objectRequest, entry.getKey(), entry.getValue(), table))
        .forEach(nestedSelect -> addSubSelect(dataQuery, nestedSelect));

    objectRequest.getNestedObjectFields()
        .forEach((key, value) -> {
          var objectField = objectType.getFields()
              .get(key.getName());

          var nestedObjectMapper = new ObjectMapper();

          fieldMapper.register(objectField.getName(), nestedObjectMapper);

          value.stream()
              .map(selectedField -> processScalarField(selectedField, objectField.getTargetType(), table,
                  nestedObjectMapper))
              .forEach(dataQuery::addSelect);
        });

    objectRequest.getAggregateObjectFields()
        .stream()
        .flatMap(aggregateObjectFieldConfiguration -> processAggregateObjectField(objectRequest,
            aggregateObjectFieldConfiguration, table))
        .collect(Collectors.toList())
        .forEach(nestedSelect -> addSubSelect(dataQuery, nestedSelect));

    objectRequest.getSelectedObjectListFields()
        .entrySet()
        .stream()
        .flatMap(entry -> processObjectListFields(objectRequest, entry.getKey()
            .getName(), entry.getValue(), table))
        .forEach(dataQuery::addSelect);

    return dataQuery;
  }

  private void addSubSelect(SelectQuery<Record> selectQuery, Select<Record> nestedSelectQuery) {
    var nestedTable = nestedSelectQuery.asTable(aliasManager.newAlias());

    var lateralTable = DSL.lateral(nestedTable);
    selectQuery.addSelect(DSL.field(String.format("\"%s\".*", lateralTable.getName())));
    selectQuery.addJoin(lateralTable, JoinType.LEFT_OUTER_JOIN);
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

    join(table, objectField, null).forEach(subSelect::addConditions);

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


  private SelectQuery<Record> batchJoin(SelectQuery<Record> dataQuery, Table<Record> dataTable,
      JoinCriteria joinCriteria, List<JoinColumn> joinColumns) {
    var objectType = (PostgresObjectType) requestContext.getObjectField()
        .getObjectType();

    // Create virtual table with static key values
    var keyTable = createValuesTable(objectType, joinColumns, joinCriteria.getKeys());

    var batchQuery = dslContext.selectQuery(keyTable);

    dataQuery.addConditions(createJoinConditions(dataTable, keyTable, joinColumns, objectType));

    addExists(dataQuery, joinColumns, dataTable);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);

    return batchQuery;
  }

  private SelectQuery<Record> batchJoin(SelectQuery<Record> dataQuery, Table<Record> dataTable,
      JoinCriteria joinCriteria, PostgresObjectType targetObjectType) {
    var objectField = (PostgresObjectField) requestContext.getObjectField();
    var objectType = (PostgresObjectType) objectField.getObjectType();
    var joinTable = objectField.getJoinTable();

    // Create virtual table with static key values
    var keyTable = createValuesTable(objectType, joinTable.getJoinColumns(), joinCriteria.getKeys());

    var junctionTable = DSL.table(joinTable.getName())
        .as(aliasManager.newAlias());

    dataQuery.addFrom(junctionTable);
    dataQuery.addConditions(createJoinConditions(junctionTable, keyTable, joinTable.getJoinColumns(), objectType));

    addExists(dataQuery, joinTable.getJoinColumns(), junctionTable);

    dataQuery.addConditions(
        createJoinConditions(junctionTable, dataTable, joinTable.getInverseJoinColumns(), targetObjectType));

    var batchQuery = dslContext.selectQuery(keyTable);

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);

    return batchQuery;
  }

  private void addExists(SelectQuery<Record> dataQuery, List<JoinColumn> joinColumns, Table<Record> table) {
    var existsColumnNames = joinColumns.stream()
        .map(JoinColumn::getName)
        .collect(Collectors.toList());

    existsColumnNames.stream()
        .map(existsColumn -> QueryHelper.column(table, existsColumn))
        .forEach(dataQuery::addSelect);

    // Register field mapper for exist row columns
    fieldMapper.register(EXISTS_KEY, row -> existsColumnNames.stream()
        .filter(key -> !Objects.isNull(row.get(key)))
        .collect(Collectors.toMap(Function.identity(), row::get, (prev, next) -> next, HashMap::new)));
  }

  private Table<Record> createValuesTable(PostgresObjectType objectType, List<JoinColumn> joinColumns,
      Collection<Map<String, Object>> keys) {
    var keyColumnNames = joinColumns.stream()
        .map(joinColumn -> columnName(joinColumn, objectType))
        .collect(Collectors.toList());

    var keyTableRows = keys.stream()
        .map(joinKey -> keyColumnNames.stream()
            .map(joinKey::get)
            .toArray())
        .map(DSL::row)
        .toArray(RowN[]::new);

    // Register field mapper for grouping rows per key
    fieldMapper.register(GROUP_KEY, row -> keyColumnNames.stream()
        .collect(Collectors.toMap(Function.identity(), row::get)));

    return DSL.values(keyTableRows)
        .as(aliasManager.newAlias(), keyColumnNames.toArray(String[]::new));
  }

  public Condition createFilterCondition(ObjectRequest objectRequest, List<PostgresObjectField> fieldPath,
      Map<String, Object> value, Table<Record> table) {
    var current = fieldPath.get(0);

    if (fieldPath.size() > 1) {
      var rest = fieldPath.subList(1, fieldPath.size());

      if (current.getTargetType()
          .isNested()) {
        return createFilterCondition(objectRequest, rest, value, table);
      }

      var filterTable = findTable((current.getTargetType()).getTable(), objectRequest.getContextCriteria())
          .as(aliasManager.newAlias());

      var filterQuery = dslContext.selectQuery(filterTable);

      filterQuery.addSelect(DSL.val(1));

      join(table, current, filterTable).forEach(filterQuery::addConditions);

      var nestedCondition = createFilterCondition(objectRequest, rest, value, filterTable);

      filterQuery.addConditions(nestedCondition);

      return DSL.exists(filterQuery);
    }

    var conditions = value.entrySet()
        .stream()
        .map(entry -> createFilterValue(entry.getKey(), current, entry.getValue()))
        .collect(Collectors.toList());

    return conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);
  }

  private List<Condition> join(Table<Record> table, PostgresObjectField current, Table<Record> relatedTable) {
    // Inverted mapped by
    if (current.getMappedByObjectField() != null) {
      var mappedByObjectField = current.getMappedByObjectField();
      return mappedByObjectField.getJoinColumns()
          .stream()
          .map(joinColumn -> {
            var field = column(relatedTable, joinColumn.getName());
            var referencedField = column(table, joinColumn, (PostgresObjectType) current.getObjectType());
            return referencedField.equal(field);
          })
          .collect(Collectors.toList());
    }

    // Normal join column
    return current.getJoinColumns()
        .stream()
        .map(joinColumn -> {
          var field = column(table, joinColumn.getName());
          var referencedField = column(relatedTable, joinColumn, current.getTargetType());
          return referencedField.equal(field);
        })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private Condition createFilterValue(String filterField, PostgresObjectField objectField, Object value) {
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
      Map<String, Object> mapValue = (Map<String, Object>) value;

      var conditions = mapValue.entrySet()
          .stream()
          .map(entry -> createFilterValue(entry.getKey(), objectField, entry.getValue()))
          .collect(Collectors.toList());

      var condition = conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);

      return DSL.not(condition);
    }

    if (SpatialConstants.GEOMETRY.equals(objectField.getType())) {
      Map<String, Object> mapValue = (Map<String, Object>) value;

      Geometry geometry = readGeometry((String) mapValue.get(SpatialConstants.FROM_WKT));

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

    throw illegalArgumentException("Unknown filter filterField '%s'", filterField);
  }

  private Geometry readGeometry(String wkt) {
    var wktReader = new WKTReader();
    try {
      return wktReader.read(wkt);
    } catch (ParseException e) {
      throw illegalArgumentException("The filter input WKT is invalid!", e);
    }
  }

  public List<SortField<Object>> createSortConditions(List<SortCriteria> sortCriterias) {
    return sortCriterias.stream()
        .map(this::createSortCondition)
        .collect(Collectors.toList());
  }

  private SortField<Object> createSortCondition(SortCriteria sortCriteria) {
    List<ObjectField> fieldPath = sortCriteria.getFields();
    var leafFieldMapper = fieldMapper.getLeafFieldMapper(fieldPath);

    var sortField = column(null, leafFieldMapper.getAlias());

    switch (sortCriteria.getDirection()) {
      case ASC:
        return sortField.asc();
      case DESC:
        return sortField.desc();
      default:
        throw unsupportedOperationException("Unsupported direction: {}", sortCriteria.getDirection());
    }
  }

  private void addPagingCriteria(SelectQuery<Record> selectQuery) {
    var source = requestContext.getSource();

    if (source == null) {
      return;
    }

    Optional<Integer> offset = Optional.ofNullable(source.get(PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME)))
        .map(Integer.class::cast);
    Optional<Integer> first = Optional.ofNullable(source.get(PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME)))
        .map(Integer.class::cast);

    if (offset.isPresent() && first.isPresent()) {
      selectQuery.addLimit(offset.get(), first.get());
    }
  }

  private List<Condition> createKeyConditions(KeyCriteria keyCriteria, PostgresObjectType objectType,
      Table<Record> table) {
    return keyCriteria.getValues()
        .entrySet()
        .stream()
        .map(entry -> objectType.getField(entry.getKey())
            .map(PostgresObjectField::getColumn)
            .map(column -> column(table, column).equal(entry.getValue()))
            .orElseThrow())
        .collect(Collectors.toList());
  }

  private SelectFieldOrAsterisk processScalarField(FieldRequest selectedField, PostgresObjectType objectType,
      Table<Record> table, ObjectFieldMapper<Map<String, Object>> objectFieldMapper) {
    var objectField = objectType.getField(selectedField.getName())
        .orElseThrow(() -> illegalStateException("Object field '{}' not found.", selectedField.getName()));

    var columnMapper = createColumnMapper(objectField.getColumn(), table);

    objectFieldMapper.register(selectedField.getName(), columnMapper);

    return columnMapper.getColumn();
  }

  private ColumnMapper createColumnMapper(String columnName, Table<Record> table) {
    var column = column(table, columnName).as(aliasManager.newAlias());

    return new ColumnMapper(column);
  }

  private Stream<SelectQuery<Record>> createNestedSelect(ObjectRequest objectRequest, FieldRequest selectedField,
      ObjectRequest nestedObjectRequest, Table<Record> table) {
    var nestedObjectAlias = aliasManager.newAlias();
    var nestedObjectMapper = new ObjectMapper(nestedObjectAlias);

    fieldMapper.register(selectedField.getName(), nestedObjectMapper);

    var nestedSelect = SelectBuilder.newSelect()
        .requestContext(requestContext)
        .fieldMapper(nestedObjectMapper)
        .aliasManager(aliasManager)
        .build(nestedObjectRequest);

    nestedSelect.addSelect(DSL.field("1")
        .as(nestedObjectAlias));

    if (!selectedField.isList()) {
      nestedSelect.addLimit(1);
    }

    var objectField = getObjectField(objectRequest, selectedField.getName());

    join(table, objectField, null).forEach(nestedSelect::addConditions);

    return Stream.of(nestedSelect);
  }

  private Stream<SelectFieldOrAsterisk> processObjectListFields(ObjectRequest objectRequest, String fieldName,
      CollectionRequest collectionRequest, Table<Record> table) {
    var objectField = getObjectField(objectRequest, fieldName);

    if (objectField.getJoinTable() != null) {
      return handleJoinTable(objectField, table);
    }

    if (objectField.getMappedBy() != null) {
      return handleJoinMappedBy(collectionRequest, objectField, table);
    }

    return Stream.of();
  }

  private Stream<SelectFieldOrAsterisk> handleJoinMappedBy(CollectionRequest collectionRequest,
      PostgresObjectField objectField, Table<Record> table) {
    var nestedObjectField = getObjectField(collectionRequest.getObjectRequest(), objectField.getMappedBy());

    // Provide join info for child data fetcher
    fieldMapper.register(JOIN_KEY_PREFIX.concat(nestedObjectField.getName()), row -> JoinCondition.builder()
        .key(getJoinColumnValues(nestedObjectField.getJoinColumns(), row))
        .build());

    return selectJoinColumns(nestedObjectField.getTargetType(), nestedObjectField.getJoinColumns(), table);
  }

  private Stream<SelectFieldOrAsterisk> handleJoinTable(PostgresObjectField objectField, Table<Record> table) {
    var joinTable = objectField.getJoinTable();

    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
        .key(getJoinColumnValues(joinTable.getJoinColumns(), row))
        .build());

    return selectJoinColumns((PostgresObjectType) objectField.getObjectType(), joinTable.getJoinColumns(), table);
  }

  private Map<String, Object> getJoinColumnValues(List<JoinColumn> joinColumns, Map<String, Object> row) {
    return joinColumns.stream()
        .collect(Collectors.toMap(JoinColumn::getReferencedField,
            joinColumn -> fieldMapper.getFieldMapper(joinColumn.getReferencedField())
                .apply(row)));
  }

  private Stream<SelectFieldOrAsterisk> selectJoinColumns(PostgresObjectType objectType, List<JoinColumn> joinColumns,
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
        });
  }

  private ColumnMapper getColumnMapper(Table<Record> table, String referencedField, PostgresObjectType objectType) {
    var objectField = objectType.getFields()
        .get(referencedField);
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
