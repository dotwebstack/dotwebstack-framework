package org.dotwebstack.framework.backend.postgres.query.objectquery;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;
import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;
import static org.jooq.impl.DSL.trueCondition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
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

  public SelectQueryBuilderResult build(CollectionQuery collectionQuery) {
    SelectQueryBuilderResult objectQueryBuilderResult = build(collectionQuery.getObjectQuery());

    SelectQuery<?> selectQuery = objectQueryBuilderResult.getQuery();

    if (collectionQuery.getPagingCriteria() != null) {
      PagingCriteria pagingCriteria = collectionQuery.getPagingCriteria();
      selectQuery.addLimit(pagingCriteria.getPage(), pagingCriteria.getPageSize());
    }

    return SelectQueryBuilderResult.builder()
        .query(selectQuery)
        .mapAssembler(objectQueryBuilderResult.getMapAssembler())
        .build();
  }

  public SelectQueryBuilderResult build(ObjectQuery objectQuery) {

    // TODO add table to selectContext? -> rename tableSelectContext
    var objectSelectContext = new ObjectSelectContext(new ObjectQueryContext());
    var fromTable = findTable(((PostgresTypeConfiguration) objectQuery.getTypeConfiguration()).getTable())
        .as(objectSelectContext.newTableAlias());
    var query = buildQuery(objectSelectContext, objectQuery, fromTable);
    var rowMapper =
        createMapAssembler(objectSelectContext.getAssembleFns(), objectSelectContext.getCheckNullAlias(), false);

    return SelectQueryBuilderResult.builder()
        .query(query)
        .mapAssembler(rowMapper)
        .build();
  }

  public SelectQuery<?> buildQuery(ObjectSelectContext objectSelectContext, ObjectQuery objectQuery,
      Table<?> fromTable) {

    SelectQuery<?> query = dslContext.selectQuery(fromTable);

    addScalarFields((PostgresTypeConfiguration) objectQuery.getTypeConfiguration(), objectQuery.getScalarFields(),
        objectSelectContext, query, fromTable);
    addNestedObjectFields(objectQuery, objectSelectContext, query, fromTable);
    addObjectFields(objectQuery, objectSelectContext, query, fromTable);
    addAggregateObjectFields(objectQuery, objectSelectContext, query, fromTable);

    if (!CollectionUtils.isEmpty(objectQuery.getKeyCriteria())) {
      return addKeyCriterias(query, objectSelectContext, fromTable, objectQuery.getKeyCriteria());
    }

    return query;
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

          Table<?> objectFieldTable = findTable(
              ((PostgresTypeConfiguration) ((AbstractFieldConfiguration) objectField.getField()).getTypeConfiguration())
                  .getTable()).asTable(objectSelectContext.newTableAlias());

          ObjectSelectContext lateralJoinContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());
          SelectQuery<?> subSelect = buildQuery(lateralJoinContext, objectField.getObjectQuery(), objectFieldTable);
          Condition condition = getJoinCondition((PostgresFieldConfiguration) objectField.getField(), fieldTable,
              (PostgresTypeConfiguration) objectField.getObjectQuery()
                  .getTypeConfiguration(),
              objectFieldTable);
          subSelect.addConditions(condition);

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

  private void addAggregateObjectFields(ObjectQuery objectQuery,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> fieldTable) {

    objectQuery.getAggregateObjectFields().forEach(aggregateObjectFieldConfiguration -> {
      ObjectSelectContext aggregateObjectSelectContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

      var stringJoinAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(true);

      var otherAggregateFields = aggregateObjectFieldConfiguration.getAggregateFields(false);

      stringJoinAggregateFields.forEach(stringJoinAggregateField ->
          processAggregateFields(List.of(stringJoinAggregateField), aggregateObjectFieldConfiguration, aggregateObjectSelectContext, query, (PostgresTypeConfiguration) objectQuery.getTypeConfiguration(), fieldTable)
      );

      if(otherAggregateFields.size() > 0) {
        processAggregateFields(otherAggregateFields, aggregateObjectFieldConfiguration, aggregateObjectSelectContext, query, (PostgresTypeConfiguration) objectQuery.getTypeConfiguration(), fieldTable);
      }

      objectSelectContext.getAssembleFns()
              .put(aggregateObjectFieldConfiguration.getField()
                      .getName(),
                  createMapAssembler(aggregateObjectSelectContext.getAssembleFns(), aggregateObjectSelectContext.getCheckNullAlias(),
                      false)::apply);

    });
  }

  private void processAggregateFields(List<AggregateFieldConfiguration> aggregateFields, AggregateObjectFieldConfiguration aggregateObjectFieldConfiguration, ObjectSelectContext aggregateObjectSelectContext, SelectQuery<?> query, PostgresTypeConfiguration mainTypeConfiguration, Table<?> fieldTable) {
    PostgresTypeConfiguration aggregateTypeConfiguration =
        (PostgresTypeConfiguration) ((AbstractFieldConfiguration) aggregateObjectFieldConfiguration.getField())
            .getTypeConfiguration();

    Table<?> aliasedAggregateTable =
        findTable(aggregateTypeConfiguration.getTable()).asTable(aggregateObjectSelectContext.newTableAlias());

    SelectQuery<?> subSelect = dslContext.selectQuery(aliasedAggregateTable);

    addAggregateFields(aggregateFields, aggregateObjectSelectContext, subSelect, aliasedAggregateTable);

    // add join condition to subselect query
    Condition condition = getJoinCondition((PostgresFieldConfiguration) aggregateObjectFieldConfiguration.getField(), aliasedAggregateTable,
        mainTypeConfiguration, fieldTable);
    subSelect.addConditions(condition);

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

          Field<?> column = aggregateFieldFactory.create(aggregateFieldConfiguration, table.getName(), columnName, columnAlias)
              .as(columnAlias);

          objectSelectContext.getAssembleFns()
              .put(aggregateFieldConfiguration.getAlias(), row -> row.get(column.getName()));

          query.addSelect(column);

          if(aggregateFieldConfiguration.getAggregateFunctionType() == JOIN && aggregateFieldConfiguration.getField().isList()) {
            query.addJoin(DSL.unnest(DSL.field(DSL.name(table.getName(), columnName), String[].class)).as(columnAlias), JoinType.CROSS_JOIN);
          }
        });
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

  private Condition getJoinCondition(PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      PostgresTypeConfiguration rightSideConfiguration, Table<?> rightSideTable) {

    return leftSideConfiguration.findJoinColumns()
        .stream()
        .map(joinColumn -> {

          var otherSideFieldConfiguration = rightSideConfiguration.getFields()
              .get(joinColumn.getField());

          var leftColumn = leftSideTable.field(joinColumn.getName(), Object.class);
          var rightColumn = rightSideTable.field(otherSideFieldConfiguration.getColumn(), Object.class);
          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }
}
