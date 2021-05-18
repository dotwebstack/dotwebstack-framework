package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;
import static org.jooq.impl.DSL.trueCondition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilderResult;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionQuery;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
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

  public ObjectQueryBuilder(DSLContext dslContext) {
    this.dslContext = dslContext;
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

    addScalarFields(objectQuery.getScalarFields(), objectSelectContext, query, fromTable);
    addNestedObjectFields(objectQuery.getNestedObjectFields(), objectSelectContext, query, fromTable);
    addObjectFields(objectQuery, objectSelectContext, query, fromTable);
    addAggregateFields(objectQuery.getAggregateObjectFields(), objectSelectContext, query, fromTable);

    if (!CollectionUtils.isEmpty(objectQuery.getKeyCriteria())) {
      return addKeyCriterias(query, objectSelectContext, fromTable, objectQuery.getKeyCriteria());
    }

    return query;
  }

  private void addScalarFields(List<FieldConfiguration> scalarFields, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> table) {

    scalarFields.forEach(scalarField -> {
      String columnAlias = objectSelectContext.newSelectAlias();
      Field<?> column = Objects.requireNonNull(table.field(((PostgresFieldConfiguration) scalarField).getColumn()))
          .as(columnAlias);
      objectSelectContext.getAssembleFns()
          .put(scalarField.getName(), row -> row.get(column.getName()));

      if (((AbstractFieldConfiguration) scalarField).isKeyField()) {
        objectSelectContext.getCheckNullAlias()
            .set(columnAlias);
      }
      query.addSelect(column);
    });
  }

  private void addNestedObjectFields(List<NestedObjectFieldConfiguration> nestedObjectFields,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> fieldTable) {

    nestedObjectFields.forEach(nestedObjectField -> {

      ObjectSelectContext nestedObjectContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());
      addScalarFields(nestedObjectField.getScalarFields(), nestedObjectContext, query, fieldTable);
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

  private void addAggregateFields(List<AggregateObjectFieldConfiguration> aggregateObjectFields,
      ObjectSelectContext objectSelectContext, SelectQuery<?> query, Table<?> fieldTable) {

  }

  private SelectQuery<?> addKeyCriterias(SelectQuery<?> query, ObjectSelectContext objectSelectContext,
      Table<?> fieldTable, List<KeyCriteria> keyCriterias) {
    RowN[] valuesTableRows = keyCriterias.stream()
        .map(keyCriteria -> DSL.row(keyCriteria.getValues()
            .values()))
        .toArray(RowN[]::new);

    Map<String, String> keyColumnNames = keyCriterias.stream()
        .findAny()
        .orElseThrow()
        .getValues()
        .keySet()
        .stream()
        .collect(Collectors.toMap(Function.identity(), keyColumnName -> objectSelectContext.newSelectAlias()));

    Table<Record> valuesTable = DSL.values(valuesTableRows)
        .as(objectSelectContext.newTableAlias(), keyColumnNames.values()
            .toArray(String[]::new));

    var joinCondition = keyColumnNames.entrySet()
        .stream()
        .map(entry -> DSL.field(DSL.name(fieldTable.getName(), entry.getKey()))
            .eq(DSL.field(DSL.name(valuesTable.getName(), entry.getValue()))))
        .reduce(DSL.noCondition(), Condition::and);


    Table<?> lateralTable = DSL.lateral(fieldTable.where(joinCondition))
        .asTable(objectSelectContext.newTableAlias());

    List<Field<Object>> selectedColumns = Stream.concat(keyColumnNames.values()
        .stream()
        .map(DSL::field),
        Set.of(DSL.field(lateralTable.getName()
            .concat(".*")))
            .stream())
        .collect(Collectors.toList());

    SelectQuery<?> valuesQuery = dslContext.selectQuery();

    valuesQuery.addSelect(selectedColumns);
    valuesQuery.addFrom(valuesTable.leftJoin(lateralTable)
        .on(trueCondition()));

    return valuesQuery;
  }

  private Table<?> findTable(String name) {

    // TODO add to PostgresTypeConfig
    String[] path = name.split("\\.");
    var tables = dslContext.meta()
        .getTables(path[path.length - 1]);

    return tables.get(0);
  }

  private Condition getJoinCondition(PostgresFieldConfiguration fieldConfiguration, Table<?> fieldTable,
      PostgresTypeConfiguration otherSideTypeConfiguration, Table<?> otherSideTable) {

    // TODO invert (for aggregate) needs to be resolved in configuration
    return fieldConfiguration.findJoinColumns()
        .stream()
        .map(joinColumn -> {

          var otherSideFieldConfiguration = otherSideTypeConfiguration.getFields()
              .get(joinColumn.getField());
          var leftColumn = fieldTable.field(joinColumn.getName(), Object.class);
          var rightColumn = otherSideTable.field(otherSideFieldConfiguration.getColumn(), Object.class);
          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }
}
