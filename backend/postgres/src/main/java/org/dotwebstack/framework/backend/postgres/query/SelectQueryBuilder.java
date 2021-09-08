package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createMapAssembler;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.field;
import static org.dotwebstack.framework.backend.postgres.query.TableHelper.findTable;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.model.PostgresObjectRequestFactory;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.ScalarField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.origin.Origin;
import org.dotwebstack.framework.core.query.model.origin.Requested;
import org.dotwebstack.framework.core.query.model.origin.Sorting;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class SelectQueryBuilder {

  private final DSLContext dslContext;

  private final AggregateFieldFactory aggregateFieldFactory;

  private final JoinHelper joinHelper;

  private final FilterConditionHelper filterConditionHelper;

  public SelectQueryBuilder(DSLContext dslContext, AggregateFieldFactory aggregateFieldFactory, JoinHelper joinHelper,
      FilterConditionHelper filterConditionHelper) {
    this.dslContext = dslContext;
    this.aggregateFieldFactory = aggregateFieldFactory;
    this.joinHelper = joinHelper;
    this.filterConditionHelper = filterConditionHelper;
  }

  public SelectQueryBuilderResult build(CollectionRequest collectionRequest) {
    return build(collectionRequest, new ObjectSelectContext());
  }

  public SelectQueryBuilderResult build(CollectionRequest collectionRequest, ObjectSelectContext objectSelectContext) {
    var objectRequest = collectionRequest.getObjectRequest();

    var fromTable = findTable(((PostgresTypeConfiguration) objectRequest.getTypeConfiguration()).getTable(),
        objectRequest.getContextCriterias()).as(objectSelectContext.newTableAlias());

    var postgresObjectRequest = PostgresObjectRequestFactory.create(objectRequest, collectionRequest.getSortCriterias(),
        objectSelectContext.getObjectQueryContext());

    var selectQuery = buildQuery(objectSelectContext, postgresObjectRequest, fromTable);

    if (!CollectionUtils.isEmpty(collectionRequest.getSortCriterias())) {
      createSortConditions(collectionRequest.getSortCriterias(), objectSelectContext, fromTable)
          .forEach(selectQuery::addOrderBy);
    }

    collectionRequest.getFilterCriterias()
        .stream()
        .map(filterCriteria -> filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext,
            objectRequest))
        .forEach(selectQuery::addConditions);

    if (collectionRequest.getPagingCriteria() != null) {
      var pagingCriteria = collectionRequest.getPagingCriteria();
      selectQuery.addLimit(pagingCriteria.getOffset(), pagingCriteria.getFirst());
    }

    if (!CollectionUtils.isEmpty(objectRequest.getKeyCriteria())) {
      selectQuery = addKeyCriterias(selectQuery, objectSelectContext, fromTable, objectRequest);
    }

    var rowMapper = createMapAssembler(objectSelectContext.getAssembleFns(), objectSelectContext.getCheckNullAlias(),
        objectSelectContext.isUseNullMapWhenNotFound());

    return SelectQueryBuilderResult.builder()
        .query(selectQuery)
        .mapAssembler(rowMapper)
        .context(objectSelectContext)
        .table(fromTable)
        .build();
  }

  public SelectQueryBuilderResult build(ObjectRequest objectRequest) {
    return build(objectRequest, new ObjectSelectContext());
  }

  public SelectQueryBuilderResult build(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext) {
    var fromTable = findTable(((PostgresTypeConfiguration) objectRequest.getTypeConfiguration()).getTable(),
        objectRequest.getContextCriterias()).as(objectSelectContext.newTableAlias());
    return build(objectRequest, objectSelectContext, fromTable);
  }

  private SelectQueryBuilderResult build(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      Table<Record> fromTable) {
    var query = buildQuery(objectSelectContext, objectRequest, fromTable);

    var rowMapper = createMapAssembler(objectSelectContext.getAssembleFns(), objectSelectContext.getCheckNullAlias(),
        objectSelectContext.isUseNullMapWhenNotFound());

    if (!CollectionUtils.isEmpty(objectRequest.getKeyCriteria())) {
      query = addKeyCriterias(query, objectSelectContext, fromTable, objectRequest);
    }

    return SelectQueryBuilderResult.builder()
        .query(query)
        .mapAssembler(rowMapper)
        .context(objectSelectContext)
        .table(fromTable)
        .build();
  }

  private SelectQuery<?> buildQuery(ObjectSelectContext objectSelectContext, ObjectRequest objectRequest,
      Table<?> fromTable) {

    var query = dslContext.selectQuery(fromTable);

    addScalarFields((PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), objectRequest.getScalarFields(),
        objectSelectContext, query, fromTable);
    addNestedObjectFields(objectRequest, objectSelectContext, query, fromTable);
    addObjectFields(objectRequest, objectSelectContext, query, fromTable);
    addAggregateObjectFields(objectRequest, objectSelectContext, query, fromTable);

    // check if any non-key-fields need to be added in order to support join
    addReferenceColumns(objectRequest, objectSelectContext, query, fromTable);

    return query;
  }

  private void addScalarFields(PostgresTypeConfiguration typeConfiguration, List<ScalarField> scalarFields,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> table) {

    var keyFieldAdded = new AtomicBoolean(false);
    scalarFields.forEach(scalarField -> addScalarField(scalarField, objectSelectContext, query, table, keyFieldAdded));

    if (!keyFieldAdded.get() && !typeConfiguration.getKeys()
        .isEmpty()) {

      var name = typeConfiguration.getKeys()
          .get(0)
          .getField();

      var fieldConfiguration = typeConfiguration.getFields()
          .get(name);

      var scalarField = ScalarField.builder()
          .field(fieldConfiguration)
          .origins(Sets.newHashSet(Origin.requested()))
          .build();

      addScalarField(scalarField, objectSelectContext, query, table, keyFieldAdded);
    }
  }

  private void addScalarField(ScalarField scalarField, ObjectSelectContext objectSelectContext, SelectQuery<?> query,
      Table<?> table, AtomicBoolean keyFieldAdded) {
    var scalarFieldConfiguration = (PostgresFieldConfiguration) scalarField.getField();

    var column = Objects.requireNonNull(field(table, scalarFieldConfiguration.getColumn()));

    if (scalarField.hasOrigin(Requested.class)) {
      var columnAlias = objectSelectContext.newSelectAlias();
      var aliasedColumn = column.as(columnAlias);
      objectSelectContext.getAssembleFns()
          .put(scalarField.getName(), row -> row.get(aliasedColumn.getName()));

      if (scalarFieldConfiguration.isKeyField()) {
        keyFieldAdded.set(true);
        objectSelectContext.getCheckNullAlias()
            .set(columnAlias);
      }

      query.addSelect(aliasedColumn);
      objectSelectContext.getFieldAliasMap()
          .put(scalarField.getName(), columnAlias);
    }

    scalarField.getOrigins()
        .stream()
        .filter(Sorting.class::isInstance)
        .map(Sorting.class::cast)
        .findFirst()
        .ifPresent(sortingOrigin -> {
          var columnAlias = objectSelectContext.newSelectAlias();
          var aliasedColumn = column.as(columnAlias);

          var sortCriteria = sortingOrigin.getSortCriteria();
          sortingOrigin.getFieldPathAliasMap()
              .put(sortCriteria.getFieldPath()
                  .getName(), columnAlias);

          query.addSelect(aliasedColumn);
          objectSelectContext.getFieldAliasMap()
              .put(scalarField.getName(), columnAlias);
        });
  }

  private void addNestedObjectFields(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectRequest.getNestedObjectFields()
        .forEach(nestedObjectField -> addNestedObjectField(objectRequest, objectSelectContext, query, fieldTable,
            nestedObjectField));
  }

  private void addNestedObjectField(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable, NestedObjectFieldConfiguration nestedObjectField) {
    var nestedObjectContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

    addScalarFields((PostgresTypeConfiguration) objectRequest.getTypeConfiguration(),
        nestedObjectField.getScalarFields(), nestedObjectContext, query, fieldTable);

    objectSelectContext.getAssembleFns()
        .put(nestedObjectField.getField()
            .getName(),
            createMapAssembler(nestedObjectContext.getAssembleFns(), nestedObjectContext.getCheckNullAlias(),
                false)::apply);
  }

  private void addObjectFields(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectRequest.getObjectFields()
        .forEach(objectField -> addObjectField(objectRequest, objectSelectContext, query, fieldTable, objectField));
  }

  private void addObjectField(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable, ObjectFieldConfiguration objectField) {
    var lateralJoinContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

    var objectFieldConfiguration = (PostgresFieldConfiguration) objectField.getField();

    var objectFieldTable =
        findTable(((PostgresTypeConfiguration) objectFieldConfiguration.getTypeConfiguration()).getTable(),
            objectRequest.getContextCriterias()).asTable(objectSelectContext.newTableAlias());

    var subSelect = buildQuery(lateralJoinContext, objectField.getObjectRequest(), objectFieldTable);

    subSelect.addLimit(1);

    var lateralTable = subSelect.asTable(objectSelectContext.newTableAlias(objectFieldConfiguration.getName()));

    query.addSelect(lateralTable.asterisk());

    var leftSide = PostgresTableField.builder()
        .fieldConfiguration(objectFieldConfiguration)
        .table(objectFieldTable)
        .build();

    var rightSide = PostgresTableType.builder()
        .typeConfiguration((PostgresTypeConfiguration) objectRequest.getTypeConfiguration())
        .table(fieldTable)
        .build();

    joinHelper.addJoinTableCondition(subSelect, lateralJoinContext, leftSide, rightSide, Map.of(),
        objectRequest.getContextCriterias());

    List<Condition> joinConditions =
        joinHelper.createJoinConditions(objectFieldConfiguration, objectFieldTable, fieldTable, Map.of());

    subSelect.addConditions(joinConditions);
    query.addJoin(lateralTable, JoinType.OUTER_APPLY);

    objectSelectContext.getAssembleFns()
        .put(objectField.getField()
            .getName(),
            createMapAssembler(lateralJoinContext.getAssembleFns(), lateralJoinContext.getCheckNullAlias(),
                false)::apply);
  }

  private void addAggregateObjectFields(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectRequest.getAggregateObjectFields()
        .forEach(aggregateObjectFieldConfiguration -> addAggregateObjectField(objectRequest, objectSelectContext, query,
            fieldTable, aggregateObjectFieldConfiguration));
  }

  private void addAggregateObjectField(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable, AggregateObjectFieldConfiguration aggregateObjectFieldConfiguration) {
    var aggregateObjectSelectContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

    var stringJoinAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(true);

    var otherAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(false);

    stringJoinAggregateFields
        .forEach(stringJoinAggregateField -> processAggregateFields(List.of(stringJoinAggregateField),
            aggregateObjectFieldConfiguration, aggregateObjectSelectContext, query,
            (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), fieldTable,
            objectRequest.getContextCriterias()));

    if (!otherAggregateFields.isEmpty()) {
      processAggregateFields(otherAggregateFields, aggregateObjectFieldConfiguration, aggregateObjectSelectContext,
          query, (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), fieldTable,
          objectRequest.getContextCriterias());
    }

    objectSelectContext.getAssembleFns()
        .put(aggregateObjectFieldConfiguration.getField()
            .getName(),
            createMapAssembler(aggregateObjectSelectContext.getAssembleFns(),
                aggregateObjectSelectContext.getCheckNullAlias(), false)::apply);
  }

  private void processAggregateFields(List<AggregateFieldConfiguration> aggregateFields,
      AggregateObjectFieldConfiguration aggregateObjectFieldConfiguration,
      ObjectSelectContext aggregateObjectSelectContext, SelectQuery<?> query,
      PostgresTypeConfiguration mainTypeConfiguration, Table<?> fieldTable, List<ContextCriteria> contextCriterias) {
    var aggregateFieldConfiguration = (PostgresFieldConfiguration) aggregateObjectFieldConfiguration.getField();
    var aggregateTypeConfiguration = (PostgresTypeConfiguration) aggregateFieldConfiguration.getTypeConfiguration();

    var aliasedAggregateTable = findTable(aggregateTypeConfiguration.getTable(), contextCriterias)
        .asTable(aggregateObjectSelectContext.newTableAlias());

    var subSelect = dslContext.selectQuery(aliasedAggregateTable);

    addAggregateFields(aggregateFields, aggregateObjectSelectContext, subSelect, aliasedAggregateTable);

    var leftSide = PostgresTableField.builder()
        .fieldConfiguration(aggregateFieldConfiguration)
        .table(aliasedAggregateTable)
        .build();

    var rightSide = PostgresTableType.builder()
        .typeConfiguration(mainTypeConfiguration)
        .table(fieldTable)
        .build();

    // add join condition to subselect query
    joinHelper.addAggregateJoin(subSelect, aggregateObjectSelectContext, leftSide, rightSide, contextCriterias);

    // join with query
    var lateralTable = subSelect.asTable(aggregateObjectSelectContext.newTableAlias());
    query.addSelect(lateralTable.asterisk());
    query.addJoin(lateralTable, JoinType.OUTER_APPLY);
  }

  private void addAggregateFields(List<AggregateFieldConfiguration> aggregateFieldConfigurations,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> table) {
    aggregateFieldConfigurations.forEach(aggregateFieldConfiguration -> addAggregateField(objectSelectContext, query,
        table, aggregateFieldConfiguration));
  }

  private void addAggregateField(ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> table,
      AggregateFieldConfiguration aggregateFieldConfiguration) {
    var columnAlias = objectSelectContext.newSelectAlias();
    var columnName = ((PostgresFieldConfiguration) aggregateFieldConfiguration.getField()).getColumn();

    var column = aggregateFieldFactory.create(aggregateFieldConfiguration, table.getName(), columnName, columnAlias)
        .as(columnAlias);

    objectSelectContext.getAssembleFns()
        .put(aggregateFieldConfiguration.getAlias(), row -> row.get(column.getName()));

    query.addSelect(column);

    if (aggregateFieldConfiguration.getAggregateFunctionType() == JOIN && aggregateFieldConfiguration.getField()
        .isList()) {
      query.addJoin(DSL.unnest(DSL.field(DSL.name(table.getName(), columnName), String[].class))
          .as(columnAlias), JoinType.CROSS_JOIN);
    }
  }

  private void addReferenceColumns(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> table) {
    if (!objectRequest.getObjectFields()
        .isEmpty()
        || !objectRequest.getAggregateObjectFields()
            .isEmpty()
        || !objectRequest.getCollectionObjectFields()
            .isEmpty()) {
      var typeConfiguration = (PostgresTypeConfiguration) objectRequest.getTypeConfiguration();
      typeConfiguration.getReferencedColumns()
          .values()
          .forEach(referenceFieldConfiguration -> {
            var refScalarField = ScalarField.builder()
                .field(referenceFieldConfiguration)
                .origins(Sets.newHashSet(Origin.requested()))
                .build();
            addScalarField(refScalarField, objectSelectContext, query, table, new AtomicBoolean());
          });
    }
  }

  private SelectQuery<?> addKeyCriterias(SelectQuery<?> subSelectQuery, ObjectSelectContext objectSelectContext,
      Table<Record> fieldTable, ObjectRequest objectRequest) {

    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) objectRequest.getTypeConfiguration();

    Optional<Table<Record>> joinTable =
        joinHelper.createJoinTableForKeyCriteria((PostgresTypeConfiguration) objectRequest.getTypeConfiguration(),
            subSelectQuery, objectSelectContext, fieldTable, objectRequest);

    final Table<Record> referencedTable = joinTable.orElse(fieldTable);

    // create value rows array
    var valuesTableRows = objectRequest.getKeyCriteria()
        .stream()
        .map(keyCriteria -> DSL.row(keyCriteria.getValues()
            .values()))
        .toArray(RowN[]::new);

    // create key column names map
    var keyCriteria = objectRequest.getKeyCriteria()
        .stream()
        .findAny()
        .orElseThrow();

    var keyColumnNames = keyCriteria.getValues()
        .keySet()
        .stream()
        .collect(Collectors.toMap(name -> getColumnForKeyCriteria(typeConfiguration, keyCriteria, name),
            name -> objectSelectContext.newSelectAlias()));

    objectSelectContext.setKeyColumnNames(keyColumnNames);

    // create virtual table
    var valuesTable = DSL.values(valuesTableRows)
        .as(objectSelectContext.newTableAlias(), keyColumnNames.values()
            .toArray(String[]::new));

    // create joinCondition from subselect keycriteria values
    var joinCondition = keyColumnNames.entrySet()
        .stream()
        .map(entry -> field(referencedTable, entry.getKey()).eq(field(valuesTable, entry.getValue())))
        .reduce(DSL.noCondition(), Condition::and);

    subSelectQuery.addConditions(joinCondition);

    // create select query for given keyCriteria and subSelectQuery
    var query = dslContext.selectQuery();

    var lateralTable = subSelectQuery.asTable(objectSelectContext.newTableAlias());

    query.addFrom(valuesTable);
    query.addSelect(lateralTable.asterisk());
    query.addJoin(lateralTable, JoinType.OUTER_APPLY);

    query.addSelect(keyColumnNames.values()
        .stream()
        .map(DSL::field)
        .collect(Collectors.toList()));

    return query;
  }

  private String getColumnForKeyCriteria(PostgresTypeConfiguration typeConfiguration, KeyCriteria keyCriteria,
      String name) {
    // name equals column name
    if (keyCriteria instanceof PostgresKeyCriteria) {
      return name;
    }

    // name equals field name
    return typeConfiguration.getField(name)
        .filter(fieldConfiguration -> Objects.nonNull(fieldConfiguration.getColumn()))
        .map(PostgresFieldConfiguration::getColumn)
        .orElseThrow();
  }

  @SuppressWarnings("rawtypes")
  public static List<SortField> createSortConditions(List<SortCriteria> sortCriterias,
      ObjectSelectContext objectSelectContext, Table<?> fromTable) {
    return sortCriterias.stream()
        .map(sortCriteria -> createSortCondition(objectSelectContext, fromTable, sortCriteria))
        .collect(Collectors.toList());
  }

  private static SortField<?> createSortCondition(ObjectSelectContext objectSelectContext, Table<?> fromTable,
      SortCriteria sortCriteria) {
    var sortTable = !sortCriteria.getFieldPath()
        .isLeaf() ? objectSelectContext.getTableAlias(
            sortCriteria.getFieldPath()
                .getFieldConfiguration()
                .getName())
            : fromTable.getName();

    var sortColumn = objectSelectContext.getObjectQueryContext()
        .getFieldPathAliasMap()
        .get(sortCriteria.getFieldPath()
            .getName());

    if (sortColumn == null) {
      PostgresFieldConfiguration postgresFieldConfiguration = (PostgresFieldConfiguration) sortCriteria.getFieldPath()
          .getFieldConfiguration();
      sortColumn = postgresFieldConfiguration.getColumn();
    }

    Field<?> sortField = DSL.field(DSL.name(sortTable, sortColumn));

    switch (sortCriteria.getDirection()) {
      case ASC:
        return sortField.asc();
      case DESC:
        return sortField.desc();
      default:
        throw unsupportedOperationException("Unsupported direction: {}", sortCriteria.getDirection());
    }
  }
}
