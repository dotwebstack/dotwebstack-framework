package org.dotwebstack.framework.backend.postgres.query;

// import static
// org.dotwebstack.framework.backend.postgres.query.FilterConditionHelper.createFilterConditions;
import static org.dotwebstack.framework.backend.postgres.query.FilterConditionHelper.createFilterConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createMapAssembler;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.model.Origin;
import org.dotwebstack.framework.backend.postgres.query.model.PostgresObjectRequestFactory;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.RowN;
import org.jooq.SelectQuery;
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

    var fromTable = findTable(((PostgresTypeConfiguration) objectRequest.getTypeConfiguration()).getTable())
        .as(objectSelectContext.newTableAlias());

    // optie 1:
    // objectRequest bewerken zodat je een join afdwingt tbv filter
    // strat 1: we gebruiken huidige left join en voeg conditie toe aan de buitenste query
    // je hebt tableAlias en columnAlias nodig voor address.city
    // strat 2: we maken een aparte 'filter' join (inner) zodat we geen gebruik hoeven te maken van
    // aliassen

    // generate objectfieldconfiguration and add to object request
    // ObjectFieldConfiguration needs an indicator that it is used for filtering, origins: [REQUESTED,
    // FILTERING, SORTING]
    // of
    // extend objectrequest: PostgresObjectRequest en PostgresObjectFieldConfiguration with indicator
    // usedForFiltering
    // de gefilterde kolommen worden niet gealised
    var postgresObjectRequest = PostgresObjectRequestFactory.create(objectRequest);
    postgresObjectRequest.addFilterCriteria(collectionRequest.getFilterCriterias());

    var selectQuery = buildQuery(objectSelectContext, postgresObjectRequest, fromTable);

    if (!CollectionUtils.isEmpty(collectionRequest.getFilterCriterias())) {
      // filterConditionHelper.createFilterConditions(collectionRequest.getFilterCriterias(), selectQuery,
      // fromTable, (PostgresTypeConfiguration) objectRequest.getTypeConfiguration());

      // visitAddress.city
      // is er een join met address en bevat de select de kolom city
      createFilterConditions(collectionRequest.getFilterCriterias(), objectSelectContext, fromTable)
          .forEach(selectQuery::addConditions);
    }

    if (collectionRequest.getPagingCriteria() != null) {
      var pagingCriteria = collectionRequest.getPagingCriteria();
      selectQuery.addLimit(pagingCriteria.getPage(), pagingCriteria.getPageSize());
    }

    if (!CollectionUtils.isEmpty(objectRequest.getKeyCriteria())) {
      selectQuery = addKeyCriterias(selectQuery, objectSelectContext, fromTable, objectRequest.getKeyCriteria());
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
    var fromTable = findTable(((PostgresTypeConfiguration) objectRequest.getTypeConfiguration()).getTable())
        .as(objectSelectContext.newTableAlias());
    return build(objectRequest, objectSelectContext, fromTable);
  }

  private SelectQueryBuilderResult build(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      Table<?> fromTable) {
    var query = buildQuery(objectSelectContext, objectRequest, fromTable);

    var rowMapper = createMapAssembler(objectSelectContext.getAssembleFns(), objectSelectContext.getCheckNullAlias(),
        objectSelectContext.isUseNullMapWhenNotFound());

    if (!CollectionUtils.isEmpty(objectRequest.getKeyCriteria())) {
      query = addKeyCriterias(query, objectSelectContext, fromTable, objectRequest.getKeyCriteria());
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

    // add inner join if this subselect with jointable
    addJoinTableJoin((PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), query, objectSelectContext,
        fromTable);
    return query;
  }

  private void addJoinTableJoin(PostgresTypeConfiguration typeConfiguration, SelectQuery<?> query,
      ObjectSelectContext objectSelectContext, Table<?> table) {
    if (!objectSelectContext.getJoinCriteria()
        .isEmpty()) {
      var joinCriteria = objectSelectContext.getJoinCriteria();
      var postgresKeyCriteria = joinCriteria.get(0);
      var joinTable = postgresKeyCriteria.getJoinTable();
      var aliasedJoinTable = findTable(joinTable.getName()).asTable(objectSelectContext.newTableAlias());

      var joinCondition = createJoinTableJoinCondition(joinTable.getInverseJoinColumns(), typeConfiguration.getFields(),
          aliasedJoinTable, table);

      query.addJoin(aliasedJoinTable, JoinType.JOIN, joinCondition);

      // create where condition
      addJoinTableWhereCondition(query, objectSelectContext, joinCriteria, aliasedJoinTable);
    }
  }

  private void addJoinTableWhereCondition(SelectQuery<?> query, ObjectSelectContext objectSelectContext,
      List<PostgresKeyCriteria> joinCriteria, Table<?> aliasedJoinTable) {
    var keyColumnNames = new HashMap<String, String>();
    var valuesPerKeyIdentifier = getKeyValuesPerKeyIdentifier(joinCriteria);
    valuesPerKeyIdentifier.forEach((keyColumnName, value) -> {
      var keyColumnAlias = objectSelectContext.newSelectAlias();
      var keyColumn = aliasedJoinTable.field(keyColumnName, Object.class)
          .as(keyColumnAlias);
      query.addSelect(keyColumn);
      // add IN condition
      var inCondition = createJoinTableInCondition(aliasedJoinTable, keyColumnName, value);
      query.addConditions(inCondition);
      keyColumnNames.put(keyColumnName, keyColumnAlias);
    });

    // add setKeyColumnNames
    objectSelectContext.setKeyColumnNames(keyColumnNames);
  }

  private Condition createJoinTableJoinCondition(List<JoinColumn> joinColumns,
      Map<String, PostgresFieldConfiguration> fields, Table<?> leftSideTable, Table<?> rightSideTable) {

    return joinColumns.stream()
        .map(joinColumn -> {

          var otherSideFieldConfiguration = fields.get(joinColumn.getField());

          var leftColumn = leftSideTable.field(joinColumn.getName(), Object.class);
          var rightColumn = rightSideTable.field(otherSideFieldConfiguration.getColumn(), Object.class);
          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }

  private Condition createJoinTableInCondition(Table<?> joinTable, String keyColumnName, List<Object> values) {
    var leftColumn = DSL.field(DSL.name(joinTable.getName(), keyColumnName));
    return Objects.requireNonNull(leftColumn)
        .in(values);
  }

  private Map<String, List<Object>> getKeyValuesPerKeyIdentifier(List<PostgresKeyCriteria> joinCriteria) {
    var keyValuesPerKeyIdentifier = new HashMap<String, List<Object>>();
    joinCriteria.forEach(criteria -> criteria.getValues()
        .forEach((key, value) -> {
          if (keyValuesPerKeyIdentifier.containsKey(key)) {
            var values = keyValuesPerKeyIdentifier.get(key);
            values.add(value);
            keyValuesPerKeyIdentifier.put(key, values);
          } else {
            keyValuesPerKeyIdentifier.put(key, new ArrayList<>(Arrays.asList(value)));
          }
        }));
    return keyValuesPerKeyIdentifier;
  }

  private void addScalarFields(PostgresTypeConfiguration typeConfiguration, List<FieldConfiguration> scalarFields,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> table) {

    var keyFieldAdded = new AtomicBoolean(false);
    scalarFields.forEach(scalarField -> addScalarField(scalarField, objectSelectContext, query, table, keyFieldAdded));

    if (!keyFieldAdded.get() && !typeConfiguration.getKeys()
        .isEmpty()) {

      var name = typeConfiguration.getKeys()
          .get(0)
          .getField();
      addScalarField(typeConfiguration.getFields()
          .get(name), objectSelectContext, query, table, keyFieldAdded);
    }
  }

  private void addScalarField(FieldConfiguration scalarField, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> table, AtomicBoolean keyFieldAdded) {
    var postgresScalarField = (PostgresFieldConfiguration) scalarField;
    var column = Objects.requireNonNull(table.field((postgresScalarField.getColumn())));

    if (postgresScalarField.hasOrigin(Origin.REQUESTED)) {
      var columnAlias = objectSelectContext.newSelectAlias();
      var aliasedColumn = column.as(columnAlias);
      objectSelectContext.getAssembleFns()
          .put(scalarField.getName(), row -> row.get(aliasedColumn.getName()));

      if (((AbstractFieldConfiguration) scalarField).isKeyField()) {
        keyFieldAdded.set(true);
        objectSelectContext.getCheckNullAlias()
            .set(columnAlias);
      }
      query.addSelect(aliasedColumn);
    }
    if (postgresScalarField.hasOrigin(Origin.FILTERING) || postgresScalarField.hasOrigin(Origin.SORTING)) {
      query.addSelect(column);
    }

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
        });
  }

  private void addObjectFields(ObjectRequest objectRequest, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectRequest.getObjectFields()
        .forEach(objectField -> {
          var lateralJoinContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

          var objectFieldConfiguration = (PostgresFieldConfiguration) objectField.getField();

          var objectFieldTable =
              findTable(((PostgresTypeConfiguration) objectFieldConfiguration.getTypeConfiguration()).getTable())
                  .asTable(objectSelectContext.newTableAlias());

          var subSelect = buildQuery(lateralJoinContext, objectField.getObjectRequest(), objectFieldTable);

          addJoin(subSelect, lateralJoinContext, objectFieldConfiguration, objectFieldTable,
              (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), fieldTable);

          subSelect.addLimit(1);

          var lateralTable = subSelect.asTable(objectSelectContext.newTableAlias(objectFieldConfiguration.getName()));
          query.addSelect(lateralTable.asterisk());
          query.addJoin(lateralTable, JoinType.OUTER_APPLY);

          objectSelectContext.getAssembleFns()
              .put(objectField.getField()
                  .getName(),
                  createMapAssembler(lateralJoinContext.getAssembleFns(), lateralJoinContext.getCheckNullAlias(),
                      false)::apply);
        });
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
                  (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), fieldTable));

          if (!otherAggregateFields.isEmpty()) {
            processAggregateFields(otherAggregateFields, aggregateObjectFieldConfiguration,
                aggregateObjectSelectContext, query, (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(),
                fieldTable);
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
      PostgresTypeConfiguration mainTypeConfiguration, Table<?> fieldTable) {
    var aggregateFieldConfiguration = (PostgresFieldConfiguration) aggregateObjectFieldConfiguration.getField();
    var aggregateTypeConfiguration = (PostgresTypeConfiguration) aggregateFieldConfiguration.getTypeConfiguration();

    var aliasedAggregateTable =
        findTable(aggregateTypeConfiguration.getTable()).asTable(aggregateObjectSelectContext.newTableAlias());

    var subSelect = dslContext.selectQuery(aliasedAggregateTable);

    addAggregateFields(aggregateFields, aggregateObjectSelectContext, subSelect, aliasedAggregateTable);

    // add join condition to subselect query
    addAggregateJoin(subSelect, aggregateObjectSelectContext, aggregateFieldConfiguration, aliasedAggregateTable,
        mainTypeConfiguration, fieldTable);

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
          .forEach(referenceFieldConfiguration -> addScalarField(referenceFieldConfiguration, objectSelectContext,
              query, table, new AtomicBoolean()));
    }
  }

  private void addJoin(SelectQuery<?> subSelect, ObjectSelectContext objectSelectContext,
      PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable) {
    if (leftSideConfiguration.getJoinTable() != null) {
      // wordt deze code wel geraakt?
      var joinTable = findTable(leftSideConfiguration.getJoinTable()
          .getName()).asTable(objectSelectContext.newTableAlias());
      var condition = getJoinTableCondition(leftSideConfiguration, leftSideTable, rightSideConfiguration,
          rightSideTable, joinTable);
      // create join with jointable and join condition on joinColumns and inverse joinColumn
      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    } else {
      var condition = getJoinCondition(leftSideConfiguration.getJoinColumns(),
          ((PostgresTypeConfiguration) leftSideConfiguration.getTypeConfiguration()).getFields(), rightSideTable,
          leftSideTable);
      subSelect.addConditions(condition);
    }
  }

  private void addAggregateJoin(SelectQuery<?> subSelect, ObjectSelectContext objectSelectContext,
      PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable) {
    if (leftSideConfiguration.getJoinTable() != null) {

      var joinTable = findTable(leftSideConfiguration.getJoinTable()
          .getName()).asTable(objectSelectContext.newTableAlias());
      // create join with jointable and join condition on joinColumns and inverse joinColumn
      var condition = getJoinTableCondition(leftSideConfiguration, leftSideTable, rightSideConfiguration,
          rightSideTable, joinTable);

      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    } else {
      var condition = getJoinCondition(leftSideConfiguration.getJoinColumns(), rightSideConfiguration.getFields(),
          leftSideTable, rightSideTable);
      subSelect.addConditions(condition);
    }
  }

  private SelectQuery<?> addKeyCriterias(SelectQuery<?> subSelectQuery, ObjectSelectContext objectSelectContext,
      Table<?> fieldTable, List<KeyCriteria> keyCriterias) {

    // create value rows array
    var valuesTableRows = keyCriterias.stream()
        .map(keyCriteria -> DSL.row(keyCriteria.getValues()
            .values()))
        .toArray(RowN[]::new);

    // create key column names map
    var keyColumnNames = keyCriterias.stream()
        .findAny()
        .orElseThrow()
        .getValues()
        .keySet()
        .stream()
        .collect(Collectors.toMap(Function.identity(), keyColumnName -> objectSelectContext.newSelectAlias()));

    objectSelectContext.setKeyColumnNames(keyColumnNames);

    // create virtual table
    var valuesTable = DSL.values(valuesTableRows)
        .as(objectSelectContext.newTableAlias(), keyColumnNames.values()
            .toArray(String[]::new));

    // create joinCondition from subselect keycriteria values
    var joinCondition = keyColumnNames.entrySet()
        .stream()
        .map(entry -> DSL.field(DSL.name(fieldTable.getName(), entry.getKey()))
            .eq(DSL.field(DSL.name(valuesTable.getName(), entry.getValue()))))
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

  private Table<?> findTable(String name) {
    var path = name.split("\\.");
    var tables = dslContext.meta()
        .getTables(path[path.length - 1]);

    return tables.get(0);
  }

  private Condition getJoinTableCondition(PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable, Table<?> joinTable) {

    return getJoinCondition(leftSideConfiguration.findJoinColumns(), rightSideConfiguration.getFields(), joinTable,
        rightSideTable).and(getInverseJoinCondition(leftSideConfiguration, leftSideTable, joinTable));
  }

  private Condition getJoinCondition(List<JoinColumn> joinColumns, Map<String, PostgresFieldConfiguration> fields,
      Table<?> leftSideTable, Table<?> rightSideTable) {

    return joinColumns.stream()
        .map(joinColumn -> {

          var otherSideFieldConfiguration = fields.get(joinColumn.getField());

          var leftColumn = leftSideTable.field(joinColumn.getName(), Object.class);
          var rightColumn = rightSideTable.field(otherSideFieldConfiguration.getColumn(), Object.class);
          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }

  private Condition getInverseJoinCondition(PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      Table<?> rightSideTable) {

    return leftSideConfiguration.findInverseJoinColumns()
        .stream()
        .map(joinColumn -> {

          var otherSideFieldConfiguration = (PostgresFieldConfiguration) leftSideConfiguration.getTypeConfiguration()
              .getFields()
              .get(joinColumn.getField());

          var leftColumn = rightSideTable.field(joinColumn.getName(), Object.class);
          var rightColumn = leftSideTable.field(otherSideFieldConfiguration.getColumn(), Object.class);
          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }
}
