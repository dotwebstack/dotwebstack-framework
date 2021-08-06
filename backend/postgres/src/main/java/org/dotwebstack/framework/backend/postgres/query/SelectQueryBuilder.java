package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.FilterConditionHelper.createFilterCondition;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createMapAssembler;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.field;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.model.PostgresObjectRequestFactory;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.ScalarField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.origin.Filtering;
import org.dotwebstack.framework.core.query.model.origin.Origin;
import org.dotwebstack.framework.core.query.model.origin.Requested;
import org.dotwebstack.framework.core.query.model.origin.Sorting;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
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

  public SelectQueryBuilder(DSLContext dslContext, AggregateFieldFactory aggregateFieldFactory) {
    this.dslContext = dslContext;
    this.aggregateFieldFactory = aggregateFieldFactory;
  }

  public SelectQueryBuilderResult build(CollectionRequest collectionRequest) {
    return build(collectionRequest, new ObjectSelectContext());
  }

  public SelectQueryBuilderResult build(CollectionRequest collectionRequest, ObjectSelectContext objectSelectContext) {
    var objectRequest = collectionRequest.getObjectRequest();

    var fromTable = findTable(((PostgresTypeConfiguration) objectRequest.getTypeConfiguration()).getTable(),
        objectRequest.getContextCriteria()).as(objectSelectContext.newTableAlias());

    var postgresObjectRequest = PostgresObjectRequestFactory.create(objectRequest);

    collectionRequest.getFilterCriterias()
        .forEach(postgresObjectRequest::addFields);

    collectionRequest.getSortCriterias()
        .forEach(
            sortCriteria -> postgresObjectRequest.addFields(sortCriteria, objectSelectContext.getObjectQueryContext()
                .getFieldPathAliasMap()));

    var selectQuery = buildQuery(objectSelectContext, postgresObjectRequest, fromTable);

    if (!CollectionUtils.isEmpty(collectionRequest.getFilterCriterias())) {
      collectionRequest.getFilterCriterias()
          .stream()
          .filter(filterCriteria -> filterCriteria.getFieldPaths()
              .stream()
              .anyMatch(FieldPath::isLeaf))
          .map(filterCriteria -> createFilterCondition(filterCriteria, fromTable.getName()))
          .collect(Collectors.toList())
          .forEach(selectQuery::addConditions);
    }

    if (!CollectionUtils.isEmpty(collectionRequest.getSortCriterias())) {
      createSortConditions(collectionRequest.getSortCriterias(), objectSelectContext, fromTable)
          .forEach(selectQuery::addOrderBy);
    }

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
        objectRequest.getContextCriteria()).as(objectSelectContext.newTableAlias());
    return build(objectRequest, objectSelectContext, fromTable);
  }

  private SelectQueryBuilderResult build(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      Table<?> fromTable) {
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

  private Table<?> addJoinTableJoin(PostgresTypeConfiguration typeConfiguration, SelectQuery<?> query,
      ObjectSelectContext objectSelectContext, Table<?> table, ObjectRequest objectRequest) {

    Optional<JoinTable> joinTable = objectRequest.getKeyCriteria()
        .stream()
        .filter(PostgresKeyCriteria.class::isInstance)
        .map(PostgresKeyCriteria.class::cast)
        .findFirst()
        .filter(keyCriteria -> keyCriteria.getJoinTable() != null)
        .map(PostgresKeyCriteria::getJoinTable);


    if (joinTable.isPresent()) {
      var joinTable2 = joinTable.get();

      var aliasedJoinTable = findTable(joinTable2.getName(), objectRequest.getContextCriteria())
          .asTable(objectSelectContext.newTableAlias());

      var joinCondition = createJoinTableJoinCondition(joinTable2.getInverseJoinColumns(),
          typeConfiguration.getFields(), aliasedJoinTable, table);

      query.addJoin(aliasedJoinTable, JoinType.JOIN, joinCondition);

      return aliasedJoinTable;
    }

    return null;
  }

  private Condition createJoinTableJoinCondition(List<JoinColumn> joinColumns,
      Map<String, PostgresFieldConfiguration> fields, Table<?> leftSideTable, Table<?> rightSideTable) {

    return joinColumns.stream()
        .map(joinColumn -> {
          var otherSideFieldConfiguration = fields.get(joinColumn.getField());

          var leftColumn = field(leftSideTable, joinColumn.getName());
          var rightColumn = field(rightSideTable, otherSideFieldConfiguration.getColumn());

          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
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
        .forEach(nestedObjectField -> {
          var nestedObjectContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());
          addScalarFields((PostgresTypeConfiguration) objectRequest.getTypeConfiguration(),
              nestedObjectField.getScalarFields(), nestedObjectContext, query, fieldTable);
          objectSelectContext.getAssembleFns()
              .put(nestedObjectField.getField()
                  .getName(),
                  createMapAssembler(nestedObjectContext.getAssembleFns(), nestedObjectContext.getCheckNullAlias(),
                      false)::apply);

          query.addConditions(createFilterConditions(nestedObjectField.getScalarFields(), fieldTable));
        });
  }

  private void addObjectFields(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectRequest.getObjectFields()
        .forEach(objectField -> {
          var lateralJoinContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

          var objectFieldConfiguration = (PostgresFieldConfiguration) objectField.getField();

          var objectFieldTable =
              findTable(((PostgresTypeConfiguration) objectFieldConfiguration.getTypeConfiguration()).getTable(),
                  objectRequest.getContextCriteria()).asTable(objectSelectContext.newTableAlias());

          var subSelect = buildQuery(lateralJoinContext, objectField.getObjectRequest(), objectFieldTable);

          createFilterConditions(objectField.getObjectRequest()
              .getScalarFields(), objectFieldTable).forEach(subSelect::addConditions);

          subSelect.addLimit(1);

          var lateralTable = subSelect.asTable(objectSelectContext.newTableAlias(objectFieldConfiguration.getName()));

          query.addSelect(lateralTable.asterisk());

          var joinTable = objectField.hasNestedFilteringOrigin() ? lateralTable : objectFieldTable;
          Map<String, String> fieldAliasMap =
              objectField.hasNestedFilteringOrigin() ? lateralJoinContext.getFieldAliasMap() : Map.of();

          var leftSide = PostgresTableField.builder()
              .fieldConfiguration(objectFieldConfiguration)
              .table(joinTable)
              .build();

          var rightSide = PostgresTableType.builder()
              .typeConfiguration((PostgresTypeConfiguration) objectRequest.getTypeConfiguration())
              .table(fieldTable)
              .build();

          addJoinTableCondition(subSelect, lateralJoinContext, leftSide, rightSide, fieldAliasMap,
              objectRequest.getContextCriteria());

          List<Condition> joinConditions =
              createJoinConditions(objectFieldConfiguration, joinTable, fieldTable, fieldAliasMap);

          if (objectField.hasNestedFilteringOrigin()) {
            query.addJoin(DSL.lateral(lateralTable), joinConditions.toArray(Condition[]::new));
          } else {
            subSelect.addConditions(joinConditions);
            query.addJoin(lateralTable, JoinType.OUTER_APPLY);
          }

          objectSelectContext.getAssembleFns()
              .put(objectField.getField()
                  .getName(),
                  createMapAssembler(lateralJoinContext.getAssembleFns(), lateralJoinContext.getCheckNullAlias(),
                      false)::apply);
        });
  }

  private List<Condition> createFilterConditions(List<ScalarField> scalarFields, Table<?> fieldTable) {
    return scalarFields.stream()
        .map(ScalarField::getOrigins)
        .flatMap(Collection::stream)
        .filter(Filtering.class::isInstance)
        .map(Filtering.class::cast)
        .map(filtering -> createFilterCondition(filtering.getFilterCriteria(), fieldTable.getName()))
        .collect(Collectors.toList());
  }

  private void addAggregateObjectFields(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectRequest.getAggregateObjectFields()
        .forEach(aggregateObjectFieldConfiguration -> {
          var aggregateObjectSelectContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

          var stringJoinAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(true);

          var otherAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(false);

          stringJoinAggregateFields
              .forEach(stringJoinAggregateField -> processAggregateFields(List.of(stringJoinAggregateField),
                  aggregateObjectFieldConfiguration, aggregateObjectSelectContext, query,
                  (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), fieldTable,
                  objectRequest.getContextCriteria()));

          if (!otherAggregateFields.isEmpty()) {
            processAggregateFields(otherAggregateFields, aggregateObjectFieldConfiguration,
                aggregateObjectSelectContext, query, (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(),
                fieldTable, objectRequest.getContextCriteria());
          }

          objectSelectContext.getAssembleFns()
              .put(aggregateObjectFieldConfiguration.getField()
                  .getName(),
                  createMapAssembler(aggregateObjectSelectContext.getAssembleFns(),
                      aggregateObjectSelectContext.getCheckNullAlias(), false)::apply);

        });
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
    addAggregateJoin(subSelect, aggregateObjectSelectContext, leftSide, rightSide, contextCriterias);

    // join with query
    var lateralTable = subSelect.asTable(aggregateObjectSelectContext.newTableAlias());
    query.addSelect(lateralTable.asterisk());
    query.addJoin(lateralTable, JoinType.OUTER_APPLY);
  }

  private void addAggregateFields(List<AggregateFieldConfiguration> aggregateFieldConfigurations,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> table) {
    aggregateFieldConfigurations.forEach(aggregateFieldConfiguration -> {

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
    });
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

  private void addJoinTableCondition(SelectQuery<?> subSelect, ObjectSelectContext objectSelectContext,
      PostgresTableField leftSide, PostgresTableType rightSide, Map<String, String> fieldAliasMap,
      List<ContextCriteria> contextCriterias) {

    if (leftSide.getFieldConfiguration()
        .getJoinTable() != null) {
      var joinTable = findTable(leftSide.getFieldConfiguration()
          .getJoinTable()
          .getName(), contextCriterias).asTable(objectSelectContext.newTableAlias());
      var condition = getJoinTableCondition(leftSide, rightSide, fieldAliasMap, joinTable);

      // create join for query with single value jointable mapping & subscriptions
      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    }
  }

  private List<Condition> createJoinConditions(PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      Table<?> rightSideTable, Map<String, String> fieldAliasMap) {

    if (leftSideConfiguration.getJoinColumns() != null) {
      var condition = getJoinCondition(leftSideConfiguration.getJoinColumns(),
          ((PostgresTypeConfiguration) leftSideConfiguration.getTypeConfiguration()).getFields(), rightSideTable,
          leftSideTable, fieldAliasMap);
      return List.of(condition);
    }

    return List.of();
  }

  private void addAggregateJoin(SelectQuery<?> subSelect, ObjectSelectContext objectSelectContext,
      PostgresTableField leftSide, PostgresTableType rightSide, List<ContextCriteria> contextCriterias) {

    var leftSideConfiguration = leftSide.getFieldConfiguration();
    var rightSideConfiguration = rightSide.getTypeConfiguration();

    if (leftSideConfiguration.getJoinTable() != null) {
      var joinTable = findTable(leftSideConfiguration.getJoinTable()
          .getName(), contextCriterias).asTable(objectSelectContext.newTableAlias());

      // create join with jointable and join condition on joinColumns and inverse joinColumn
      var condition = getJoinTableCondition(leftSide, rightSide, objectSelectContext.getFieldAliasMap(), joinTable);

      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    } else {
      var condition = getJoinCondition(leftSideConfiguration.getJoinColumns(), rightSideConfiguration.getFields(),
          leftSide.getTable(), rightSide.getTable(), objectSelectContext.getFieldAliasMap());
      subSelect.addConditions(condition);
    }
  }

  private SelectQuery<?> addKeyCriterias(SelectQuery<?> subSelectQuery, ObjectSelectContext objectSelectContext,
      Table<?> fieldTable, ObjectRequest objectRequest) {

    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) objectRequest.getTypeConfiguration();

    Table<?> joinTable = addJoinTableJoin((PostgresTypeConfiguration) objectRequest.getTypeConfiguration(),
        subSelectQuery, objectSelectContext, fieldTable, objectRequest);

    final Table<?> referencedTable = joinTable != null ? joinTable : fieldTable;

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

  private Table<?> createTable(String name, List<ContextCriteria> contextCriterias) {
    AtomicInteger atomicInteger = new AtomicInteger(0);

    String bindingKeys = contextCriterias.stream()
        .map(contextCriteria -> String.format("{%d}", atomicInteger.getAndIncrement()))
        .collect(Collectors.joining(","));

    Object[] bindingValues = contextCriterias.stream()
        .map(ContextCriteria::getValue)
        .collect(Collectors.toList())
        .toArray(Object[]::new);

    return DSL.table(String.format("%s_ctx(%s)", name, bindingKeys), bindingValues);
  }

  private Table<?> findTable(String name, List<ContextCriteria> contextCriteria) {
    if (!contextCriteria.isEmpty()) {
      return createTable(name, contextCriteria);
    }
    return DSL.table(DSL.name(name.split("\\.")));
  }

  private Condition getJoinTableCondition(PostgresTableField leftSide, PostgresTableType rightSide,
      Map<String, String> rightFieldAliasMap, Table<?> joinTable) {

    var joinColumns = leftSide.getFieldConfiguration()
        .findJoinColumns();

    var rightFields = rightSide.getTypeConfiguration()
        .getFields();

    return getJoinCondition(joinColumns, rightFields, joinTable, rightSide.getTable(), rightFieldAliasMap)
        .and(getInverseJoinCondition(leftSide, joinTable));
  }

  private Condition getJoinCondition(List<JoinColumn> joinColumns, Map<String, PostgresFieldConfiguration> fields,
      Table<?> leftSideTable, Table<?> rightSideTable, Map<String, String> rightFieldAliasMap) {

    return joinColumns.stream()
        .map(joinColumn -> {
          var otherSideFieldConfiguration = fields.get(joinColumn.getField());

          var leftColumn = field(leftSideTable, joinColumn.getName());

          var rightColumnAlias = rightFieldAliasMap.get(otherSideFieldConfiguration.getColumn());

          var rightColumn = field(rightSideTable,
              rightColumnAlias != null ? rightColumnAlias : otherSideFieldConfiguration.getColumn());

          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }

  private Condition getInverseJoinCondition(PostgresTableField leftPostgresTableField, Table<?> rightSideTable) {
    return leftPostgresTableField.getFieldConfiguration()
        .findInverseJoinColumns()
        .stream()
        .map(joinColumn -> {

          var otherSideFieldConfiguration = (PostgresFieldConfiguration) leftPostgresTableField.getFieldConfiguration()
              .getTypeConfiguration()
              .getFields()
              .get(joinColumn.getField());

          var leftColumn = field(rightSideTable, joinColumn.getName());
          var rightColumn = field(leftPostgresTableField.getTable(), otherSideFieldConfiguration.getColumn());

          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }

  @SuppressWarnings("rawtypes")
  public static List<SortField> createSortConditions(List<SortCriteria> sortCriterias,
      ObjectSelectContext objectSelectContext, Table<?> fromTable) {
    return sortCriterias.stream()
        .map(sortCriteria -> {
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
            PostgresFieldConfiguration postgresFieldConfiguration =
                (PostgresFieldConfiguration) sortCriteria.getFieldPath()
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
        })
        .collect(Collectors.toList());
  }

  @Builder
  @Getter
  private static class PostgresTableField {
    private final PostgresFieldConfiguration fieldConfiguration;

    private final Table<?> table;
  }

  @Builder
  @Getter
  private static class PostgresTableType {
    private final PostgresTypeConfiguration typeConfiguration;

    private final Table<?> table;
  }
}
