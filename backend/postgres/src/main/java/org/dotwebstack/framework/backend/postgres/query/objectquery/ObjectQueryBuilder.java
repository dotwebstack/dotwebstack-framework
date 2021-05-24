package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.AggregateFieldFactory;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilderResult;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionQuery;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RowN;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ObjectQueryBuilder {

  private final DSLContext dslContext;

  private final AggregateFieldFactory aggregateFieldFactory;

  public ObjectQueryBuilder(DSLContext dslContext, AggregateFieldFactory aggregateFieldFactory) {
    this.dslContext = dslContext;
    this.aggregateFieldFactory = aggregateFieldFactory;
  }

  public SelectQueryBuilderResult build(CollectionQuery collectionQuery, ObjectSelectContext objectSelectContext){
    SelectQueryBuilderResult objectQueryBuilderResult = build(collectionQuery.getObjectQuery(), objectSelectContext);

    SelectQuery<?> selectQuery = objectQueryBuilderResult.getQuery();
// TODO: fix paging criteriaheb het eindelijk functioneel werkend
//    if (collectionQuery.getPagingCriteria() != null) {
//      PagingCriteria pagingCriteria = collectionQuery.getPagingCriteria();
//      selectQuery.addLimit(pagingCriteria.getPage(), pagingCriteria.getPageSize());
//    }

    return SelectQueryBuilderResult.builder()
        .query(selectQuery)
        .mapAssembler(objectQueryBuilderResult.getMapAssembler())
        .context(objectQueryBuilderResult.getContext())
        .build();
  }

  public SelectQueryBuilderResult build(ObjectQuery objectQuery, ObjectSelectContext objectSelectContext){

    // TODO add table to selectContext? -> rename tableSelectContext
//    var objectSelectContext = new ObjectSelectContext(new ObjectQueryContext());
    var fromTable = findTable(((PostgresTypeConfiguration) objectQuery.getTypeConfiguration()).getTable())
        .as(objectSelectContext.newTableAlias());
    var query = buildQuery(objectSelectContext, objectQuery, fromTable);
    var rowMapper = createMapAssembler(objectSelectContext.getAssembleFns(), objectSelectContext.getCheckNullAlias(),
        objectSelectContext.isUseNullMapWhenNotFound());

    if (!CollectionUtils.isEmpty(objectQuery.getKeyCriteria())) {
      // TODO: if jointable in keycritera, add join table here
      query = addKeyCriterias(query, objectSelectContext, fromTable, objectQuery.getKeyCriteria());
    }

    return SelectQueryBuilderResult.builder()
        .query(query)
        .mapAssembler(rowMapper)
        .context(objectSelectContext)
        .build();
  }

  private SelectQuery<?> buildQuery(ObjectSelectContext objectSelectContext, ObjectQuery objectQuery,
      Table<?> fromTable) {

    SelectQuery<?> query = dslContext.selectQuery(fromTable);

    addScalarFields((PostgresTypeConfiguration) objectQuery.getTypeConfiguration(), objectQuery.getScalarFields(),
        objectSelectContext, query, fromTable);
    addNestedObjectFields(objectQuery, objectSelectContext, query, fromTable);
    addObjectFields(objectQuery, objectSelectContext, query, fromTable);
    addAggregateObjectFields(objectQuery, objectSelectContext, query, fromTable);

    // check if any non-key-fields need to be added in order to support join
    addReferenceColumns(objectQuery, objectSelectContext, query, fromTable);

    // add inner join if this subselect with jointable
    addJoinTableJoin((PostgresTypeConfiguration) objectQuery.getTypeConfiguration(), query, objectSelectContext,
        fromTable);
    return query;
  }

  private void addJoinTableJoin(PostgresTypeConfiguration typeConfiguration, SelectQuery<?> query,
      ObjectSelectContext objectSelectContext, Table<?> table) {
    if(!objectSelectContext.getJoinCriteria().isEmpty()) {
      List<PostgresKeyCriteria> joinCriteria = objectSelectContext.getJoinCriteria();
      PostgresKeyCriteria postgresKeyCriteria = joinCriteria.get(0);
      JoinTable joinTable = postgresKeyCriteria.getJoinTable();
      Table<?> aliasedJoinTable = findTable(joinTable.getName()).asTable(objectSelectContext.newTableAlias());

      Condition joinCondition = getJoinTableJoinCondition(joinTable.getInverseJoinColumns(), typeConfiguration.getFields(), aliasedJoinTable,
          typeConfiguration, table);
      // query.addJoin using join condition
      query.addJoin(aliasedJoinTable, JoinType.JOIN, joinCondition);

      // create where condition
      var keyColumnNames = new HashMap<String, String>();
      var valuesPerKeyIdentifier = getKeyValuesPerKeyIdentifier(joinCriteria);
      valuesPerKeyIdentifier.entrySet().stream().forEach(keyValue -> {
        String keyColumnAlias = objectSelectContext.newSelectAlias();
        String keyColumnName = keyValue.getKey();
        var keyColumn = aliasedJoinTable.field(keyColumnName, Object.class).as(keyColumnAlias);
        query.addSelect(keyColumn);
        // add IN condition
        Condition inCondition = getJoinTableWhereCondition(aliasedJoinTable, keyColumnName, keyValue.getValue());
        query.addConditions(inCondition);
        keyColumnNames.put(keyColumnName, keyColumnAlias);
      } );
      // add setKeyColumnNames
      objectSelectContext.setKeyColumnNames(keyColumnNames);

    }
  }

  private Map<String, List<Object>> getKeyValuesPerKeyIdentifier(List<PostgresKeyCriteria> joinCriteria) {
    var keyValuesPerKeyIdentifier = new HashMap<String, List<Object>>();
    joinCriteria.stream().forEach(criteria -> {
      criteria.getValues().entrySet().stream().forEach(keyValue -> {
        if(keyValuesPerKeyIdentifier.containsKey(keyValue.getKey())){
          var values = keyValuesPerKeyIdentifier.get(keyValue.getKey());
          values.add(keyValue.getValue());
          keyValuesPerKeyIdentifier.put(keyValue.getKey(), values);
        }else{
          keyValuesPerKeyIdentifier.put(keyValue.getKey(), new ArrayList<>(Arrays.asList(keyValue.getValue())));
        }
      });
    });
    return keyValuesPerKeyIdentifier;
  }

  private Condition getJoinTableWhereCondition(Table<?> joinTable, String keyColumnName, List<Object> values) {
    var leftColumn = DSL.field(DSL.name(joinTable.getName(), keyColumnName));
    var rightColumn = DSL.field(DSL.value(values));
    return Objects.requireNonNull(leftColumn)
        .in(values);
  }

  private Condition getJoinTableJoinCondition(List<JoinColumn> joinColumns, Map<String, PostgresFieldConfiguration> fields,
      Table<?> leftSideTable, PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable) {

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

  private void addScalarFields(PostgresTypeConfiguration typeConfiguration, List<FieldConfiguration> scalarFields,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> table) {

    AtomicBoolean keyFieldAdded = new AtomicBoolean(false);
    scalarFields.forEach(scalarField -> addScalarField(scalarField, objectSelectContext, query, table, keyFieldAdded));

    if (!keyFieldAdded.get() && !typeConfiguration.getKeys()
        .isEmpty()) {

      // TODO add TypeConfiguration to keys?
      String name = typeConfiguration.getKeys()
          .get(0)
          .getField();
      addScalarField(typeConfiguration.getFields()
          .get(name), objectSelectContext, query, table, keyFieldAdded);
    }
  }

  private void addScalarField(FieldConfiguration scalarField, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> table, AtomicBoolean keyFieldAdded) {
    String columnAlias = objectSelectContext.newSelectAlias();
    Field<?> column = Objects.requireNonNull(table.field(((PostgresFieldConfiguration) scalarField).getColumn()))
        .as(columnAlias);
    objectSelectContext.getAssembleFns()
        .put(scalarField.getName(), row -> row.get(column.getName()));

    if (((AbstractFieldConfiguration) scalarField).isKeyField()) {
      keyFieldAdded.set(true);
      objectSelectContext.getCheckNullAlias()
          .set(columnAlias);
    }
    query.addSelect(column);
  }

  private void addNestedObjectFields(ObjectQuery objectQuery, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectQuery.getNestedObjectFields()
        .forEach(nestedObjectField -> {

          ObjectSelectContext nestedObjectContext =
              new ObjectSelectContext(objectSelectContext.getObjectQueryContext());
          addScalarFields((PostgresTypeConfiguration) objectQuery.getTypeConfiguration(),
              nestedObjectField.getScalarFields(), nestedObjectContext, query, fieldTable);
          objectSelectContext.getAssembleFns()
              .put(nestedObjectField.getField()
                  .getName(),
                  createMapAssembler(nestedObjectContext.getAssembleFns(), nestedObjectContext.getCheckNullAlias(),
                      false)::apply);
        });
  }

  private void addObjectFields(ObjectQuery objectQuery, ObjectSelectContext objectSelectContext, SelectQuery<?> query,
      Table<?> fieldTable) {

    objectQuery.getObjectFields()
        .forEach(objectField -> {
          PostgresFieldConfiguration objectFieldConfiguration = (PostgresFieldConfiguration) objectField.getField();
          Table<?> objectFieldTable =
              findTable(((PostgresTypeConfiguration) objectFieldConfiguration.getTypeConfiguration()).getTable())
                  .asTable(objectSelectContext.newTableAlias());

          ObjectSelectContext lateralJoinContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());
          SelectQuery<?> subSelect = buildQuery(lateralJoinContext, objectField.getObjectQuery(), objectFieldTable);
          addJoin(subSelect, lateralJoinContext, objectFieldConfiguration, objectFieldTable,
              (PostgresTypeConfiguration) objectQuery.getTypeConfiguration(), fieldTable);
          subSelect.addLimit(1);

          var lateralTable = subSelect.asTable(objectSelectContext.newTableAlias());
          query.addSelect(lateralTable.asterisk());
          query.addJoin(lateralTable, JoinType.OUTER_APPLY);
          objectSelectContext.getAssembleFns()
              .put(objectField.getField()
                  .getName(),
                  createMapAssembler(lateralJoinContext.getAssembleFns(), lateralJoinContext.getCheckNullAlias(),
                      false)::apply);
        });
  }

  private void addAggregateObjectFields(ObjectQuery objectQuery, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> fieldTable) {

    objectQuery.getAggregateObjectFields()
        .forEach(aggregateObjectFieldConfiguration -> {
          ObjectSelectContext aggregateObjectSelectContext =
              new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

          var stringJoinAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(true);

          var otherAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(false);

          stringJoinAggregateFields
              .forEach(stringJoinAggregateField -> processAggregateFields(List.of(stringJoinAggregateField),
                  aggregateObjectFieldConfiguration, aggregateObjectSelectContext, query,
                  (PostgresTypeConfiguration) objectQuery.getTypeConfiguration(), fieldTable));

          if (otherAggregateFields.size() > 0) {
            processAggregateFields(otherAggregateFields, aggregateObjectFieldConfiguration,
                aggregateObjectSelectContext, query, (PostgresTypeConfiguration) objectQuery.getTypeConfiguration(),
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
    // aggregateFields [intCount, intSum]
    // aggregateObjectFieldConfiguration: beerAgg
    // aggregateObjectSelectContext (object for storing ass
    PostgresFieldConfiguration aggregateFieldConfiguration =
        (PostgresFieldConfiguration) aggregateObjectFieldConfiguration.getField();
    PostgresTypeConfiguration aggregateTypeConfiguration =
        (PostgresTypeConfiguration) aggregateFieldConfiguration.getTypeConfiguration();

    Table<?> aliasedAggregateTable =
        findTable(aggregateTypeConfiguration.getTable()).asTable(aggregateObjectSelectContext.newTableAlias());

    SelectQuery<?> subSelect = dslContext.selectQuery(aliasedAggregateTable);

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

      String columnAlias = objectSelectContext.newSelectAlias();
      String columnName = ((PostgresFieldConfiguration) aggregateFieldConfiguration.getField()).getColumn();

      Field<?> column =
          aggregateFieldFactory.create(aggregateFieldConfiguration, table.getName(), columnName, columnAlias)
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

  private void addReferenceColumns(ObjectQuery objectQuery, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> table) {
    if (!objectQuery.getObjectFields()
        .isEmpty()
        || !objectQuery.getAggregateObjectFields()
            .isEmpty()
        || !objectQuery.getCollectionObjectFields().isEmpty()) {
      PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) objectQuery.getTypeConfiguration();
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

      Table<?> joinTable = findTable(leftSideConfiguration.getJoinTable()
          .getName()).asTable(objectSelectContext.newTableAlias());
      Condition condition = getJoinTableCondition(leftSideConfiguration, leftSideTable, rightSideConfiguration,
          rightSideTable, joinTable);
      // create join with jointable and join condition on joinColumns and inverse joinColumn
      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    } else {
      Condition condition = getJoinCondition(leftSideConfiguration.getJoinColumns(),
          ((PostgresTypeConfiguration) leftSideConfiguration.getTypeConfiguration()).getFields(), rightSideTable,
          rightSideConfiguration, leftSideTable);
      subSelect.addConditions(condition);
    }
  }

  private void addAggregateJoin(SelectQuery<?> subSelect, ObjectSelectContext objectSelectContext,
      PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable) {
    if (leftSideConfiguration.getJoinTable() != null) {

      Table<?> joinTable = findTable(leftSideConfiguration.getJoinTable()
          .getName()).asTable(objectSelectContext.newTableAlias());
      // create join with jointable and join condition on joinColumns and inverse joinColumn
      Condition condition = getJoinTableCondition(leftSideConfiguration, leftSideTable, rightSideConfiguration,
          rightSideTable, joinTable);

      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    } else {
      Condition condition = getJoinCondition(leftSideConfiguration.getJoinColumns(), rightSideConfiguration.getFields(),
          leftSideTable, rightSideConfiguration, rightSideTable);
      subSelect.addConditions(condition);
    }
  }

  private SelectQuery<?> addKeyCriterias(SelectQuery<?> subSelectQuery, ObjectSelectContext objectSelectContext,
      Table<?> fieldTable, List<KeyCriteria> keyCriterias) {

    // create value rows array
    RowN[] valuesTableRows = keyCriterias.stream()
        .map(keyCriteria -> DSL.row(keyCriteria.getValues()
            .values()))
        .toArray(RowN[]::new);

    // create key column names map
    Map<String, String> keyColumnNames = keyCriterias.stream()
        .findAny()
        .orElseThrow()
        .getValues()
        .keySet()
        .stream()
        .collect(Collectors.toMap(Function.identity(), keyColumnName -> objectSelectContext.newSelectAlias()));

    objectSelectContext.setKeyColumnNames(keyColumnNames);

    // create virtual table
    Table<Record> valuesTable = DSL.values(valuesTableRows)
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
    SelectQuery<?> query = dslContext.selectQuery();

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

    // TODO add to PostgresTypeConfig
    String[] path = name.split("\\.");
    var tables = dslContext.meta()
        .getTables(path[path.length - 1]);

    return tables.get(0);
  }

  private Condition getJoinTableCondition(PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable, Table<?> joinTable) {

    return getJoinCondition(leftSideConfiguration.findJoinColumns(), rightSideConfiguration.getFields(), joinTable,
        rightSideConfiguration, rightSideTable)
            .and(getInverseJoinCondition(leftSideConfiguration, leftSideTable, rightSideConfiguration, joinTable));
  }

  private Condition getJoinCondition(List<JoinColumn> joinColumns, Map<String, PostgresFieldConfiguration> fields,
      Table<?> leftSideTable, PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable) {

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
      PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable) {

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
