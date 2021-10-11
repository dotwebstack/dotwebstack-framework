package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.Query.GROUP_KEY;
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

import graphql.schema.SelectedField;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.ObjectFieldPath;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
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
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

@Setter
@Accessors(fluent = true)
class SelectBuilder {

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  private static final DataType<Geometry> GEOMETRY_DATATYPE =
      new DefaultDataType<>(SQLDialect.POSTGRES, Geometry.class, "geometry");

  private RequestContext requestContext;

  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private AliasManager aliasManager;

  private SelectBuilder() {}

  public static SelectBuilder newSelect() {
    // TODO null checks on class properties
    return new SelectBuilder();
  }

  public SelectQuery<Record> build(CollectionRequest collectionRequest, JoinCriteria joinCriteria) {
    var dataTable = createTable(collectionRequest.getObjectRequest());
    var dataQuery = createDataQuery(collectionRequest.getObjectRequest(), dataTable);

    createSortConditions(collectionRequest.getSortCriterias(), dataTable).forEach(dataQuery::addOrderBy);

    collectionRequest.getBackendFilterCriteria()
        .stream()
        .map(filterCriteria -> createFilterCondition(collectionRequest.getObjectRequest(),
            filterCriteria.getFieldPath(), filterCriteria.getValue(), dataTable))
        .forEach(dataQuery::addConditions);

    addPagingCriteria(dataQuery);

    if (joinCriteria == null) {
      return dataQuery;
    }

    var objectField = (PostgresObjectField) requestContext.getObjectField();

    if (objectField.getJoinTable() != null) {
      return batchJoin(dataQuery, dataTable, joinCriteria);
    }

    throw new UnsupportedOperationException();
  }

  public SelectQuery<Record> build(ObjectRequest objectRequest) {
    var objectType = getObjectType(objectRequest);
    var dataTable = findTable(objectType.getTable(), objectRequest.getContextCriteria()).as(aliasManager.newAlias());

    return createDataQuery(objectRequest, dataTable);
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
        .map(selectedField -> processScalarField(selectedField, objectType, table))
        .forEach(dataQuery::addSelect);

    objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .flatMap(entry -> createNestedSelect(objectRequest, entry.getKey()
            .getName(), entry.getValue(), table))
        .forEach(nestedSelect -> {
          var lateralTable = DSL.lateral(nestedSelect.asTable(aliasManager.newAlias()));
          dataQuery.addSelect(DSL.field(String.format("\"%s\".*", lateralTable.getName())));
          dataQuery.addJoin(lateralTable, JoinType.LEFT_OUTER_JOIN);
        });

    objectRequest.getSelectedObjectListFields()
        .entrySet()
        .stream()
        .flatMap(entry -> processObjectListFields(objectRequest, entry.getKey()
            .getName(), entry.getValue(), table))
        .forEach(dataQuery::addSelect);

    return dataQuery;
  }

  private SelectQuery<Record> batchJoin(SelectQuery<Record> dataQuery, Table<Record> dataTable,
      JoinCriteria joinCriteria) {
    var objectField = (PostgresObjectField) requestContext.getObjectField();
    var joinTable = objectField.getJoinTable();

    // Create virtual table with static key values
    var keyTable = createValuesTable(joinTable, joinCriteria.getKeys());

    var batchQuery = dslContext.selectQuery(keyTable);

    var junctionTable = DSL.table(joinTable.getName())
        .as(aliasManager.newAlias());

    dataQuery.addFrom(junctionTable);
    dataQuery.addConditions(createJoinConditions(junctionTable, dataTable, joinTable.getInverseJoinColumns()));
    dataQuery.addConditions(createJoinConditions(junctionTable, keyTable, joinTable.getJoinColumns()));

    batchQuery.addJoin(DSL.lateral(dataQuery.asTable(aliasManager.newAlias())), JoinType.LEFT_OUTER_JOIN);

    return batchQuery;
  }

  private Table<Record> createValuesTable(JoinTable joinTable, Collection<Map<String, Object>> keys) {
    var keyColumnNames = joinTable.getJoinColumns()
        .stream()
        .map(JoinColumn::getReferencedColumn)
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

  public Condition createFilterCondition(ObjectRequest objectRequest, List<ObjectFieldPath> fieldPath,
      Map<String, Object> value, Table<Record> table) {
    var current = fieldPath.get(0);
    var objectField = (PostgresObjectField) current.getObjectField();

    if (fieldPath.size() > 1) {
      var filterTable =
          findTable(((PostgresObjectType) current.getObjectType()).getTable(), objectRequest.getContextCriteria())
              .as(aliasManager.newAlias());

      var filterQuery = dslContext.selectQuery(filterTable);

      filterQuery.addSelect(DSL.val(1));

      objectField.getJoinColumns()
          .forEach(joinColumn -> {
            var field = DSL.field(DSL.name(table.getName(), joinColumn.getName()));
            var referencedField = DSL.field(joinColumn.getReferencedField());
            filterQuery.addConditions(referencedField.equal(field));
          });

      var rest = fieldPath.subList(1, fieldPath.size());

      var nestedCondition = createFilterCondition(objectRequest, rest, value, filterTable);

      filterQuery.addConditions(nestedCondition);

      return DSL.exists(filterQuery);
    }

    var conditions = value.entrySet()
        .stream()
        .map(entry -> createFilterValue(entry.getKey(), objectField, entry.getValue()))
        .collect(Collectors.toList());

    return conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);
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

  @SuppressWarnings("rawtypes")
  public List<SortField> createSortConditions(List<SortCriteria> sortCriterias, Table<Record> table) {
    return sortCriterias.stream()
        .map(sortCriteria -> createSortCondition(sortCriteria, table))
        .collect(Collectors.toList());
  }

  private SortField<?> createSortCondition(SortCriteria sortCriteria, Table<Record> table) {
    if (sortCriteria.getFields()
        .size() > 1) {
      throw illegalStateException("Nested field path not supported!");
    }

    var leafObjectField = (PostgresObjectField) sortCriteria.getFields()
        .get(0);

    Field<?> sortField = DSL.field(DSL.name(table.getName(), leafObjectField.getColumn()));

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
            .map(column -> DSL.field(DSL.name(table.getName(), column))
                .equal(entry.getValue()))
            .orElseThrow())
        .collect(Collectors.toList());
  }

  private SelectFieldOrAsterisk processScalarField(SelectedField selectedField, PostgresObjectType objectType,
      Table<Record> table) {
    var objectField = objectType.getField(selectedField.getName())
        .orElseThrow(() -> illegalStateException("Object field '{}' not found.", selectedField.getName()));

    var columnMapper = createColumnMapper(objectField, table);

    fieldMapper.register(selectedField.getName(), columnMapper);

    return columnMapper.getColumn();
  }

  private ColumnMapper createColumnMapper(PostgresObjectField objectField, Table<Record> table) {
    var column = DSL.field(DSL.name(table.getName(), objectField.getColumn()))
        .as(aliasManager.newAlias());

    return new ColumnMapper(column);
  }

  private Stream<SelectQuery<Record>> createNestedSelect(ObjectRequest objectRequest, String fieldName,
      ObjectRequest nestedObjectRequest, Table<Record> table) {
    var nestedObjectAlias = aliasManager.newAlias();
    var nestedObjectMapper = new ObjectMapper(nestedObjectAlias);

    fieldMapper.register(fieldName, nestedObjectMapper);

    var nestedSelect = SelectBuilder.newSelect()
        .requestContext(requestContext)
        .fieldMapper(nestedObjectMapper)
        .aliasManager(aliasManager)
        .build(nestedObjectRequest);

    nestedSelect.addSelect(DSL.field("1")
        .as(nestedObjectAlias));

    getObjectField(objectRequest, fieldName).getJoinColumns()
        .forEach(joinColumn -> {
          var field = DSL.field(DSL.name(table.getName(), joinColumn.getName()));
          var referencedField = DSL.field(joinColumn.getReferencedField());
          nestedSelect.addConditions(referencedField.equal(field));
        });

    return Stream.of(nestedSelect);
  }

  private Stream<SelectFieldOrAsterisk> processObjectListFields(ObjectRequest objectRequest, String fieldName,
      CollectionRequest collectionRequest, Table<Record> table) {
    var objectField = getObjectField(objectRequest, fieldName);

    if (objectField.getJoinTable() != null) {
      return handleJoinTable(collectionRequest.getObjectRequest(), objectField, table);
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
    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
        .key(getJoinColumnValues(objectField.getJoinColumns(), row))
        .build());

    return selectJoinColumns(collectionRequest.getObjectRequest(), nestedObjectField.getJoinColumns(), table);
  }

  private Stream<SelectFieldOrAsterisk> handleJoinTable(ObjectRequest objectRequest, PostgresObjectField objectField,
      Table<Record> table) {
    var joinTable = objectField.getJoinTable();

    fieldMapper.register(JOIN_KEY_PREFIX.concat(objectField.getName()), row -> JoinCondition.builder()
        .key(getJoinColumnValues(joinTable.getJoinColumns(), row))
        .build());

    return selectJoinColumns(objectRequest, objectField.getJoinColumns(), table);
  }

  private Map<String, Object> getJoinColumnValues(List<JoinColumn> joinColumns, Map<String, Object> row) {
    return joinColumns.stream()
        .collect(Collectors.toMap(JoinColumn::getReferencedColumn,
            joinColumn -> fieldMapper.getFieldMapper(joinColumn.getReferencedField())
                .apply(row)));
  }

  private Stream<SelectFieldOrAsterisk> selectJoinColumns(ObjectRequest objectRequest, List<JoinColumn> joinColumns,
      Table<Record> table) {
    return joinColumns.stream()
        .map(joinColumn -> {
          var joinField = getObjectField(objectRequest, joinColumn.getReferencedField());
          var columnMapper = createColumnMapper(joinField, table);

          fieldMapper.register(joinField.getName(), columnMapper);

          return columnMapper.getColumn();
        });
  }
}
