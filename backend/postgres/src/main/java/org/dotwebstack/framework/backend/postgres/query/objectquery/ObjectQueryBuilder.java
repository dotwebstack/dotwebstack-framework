package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;

import java.util.List;
import java.util.Objects;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilderResult;
import org.dotwebstack.framework.backend.postgres.query.AggregateFieldFactory;
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
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ObjectQueryBuilder {

  private final DSLContext dslContext;

  private AggregateFieldFactory aggregateFieldFactory;

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
    addAggregateObjectFields(objectQuery.getAggregateObjectFields(), objectSelectContext, query, fromTable);

    addKeyCriteria(query, objectQuery.getKeyCriteria(), fromTable);

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

  private void addAggregateObjectFields(List<AggregateObjectFieldConfiguration> aggregateObjectFieldConfigurations, ObjectSelectContext objectSelectContext, SelectQuery<?> query,
      Table<?> fieldTable) {
    aggregateObjectFieldConfigurations.forEach(aggregateObjectFieldConfiguration -> {
      PostgresTypeConfiguration aggregateTypeConfiguration = (PostgresTypeConfiguration) ((AbstractFieldConfiguration) aggregateObjectFieldConfiguration.getField()).getTypeConfiguration();

      Table<?> aggregateTable =
          findTable(aggregateTypeConfiguration.getTable()).asTable(objectSelectContext.newTableAlias());

      SelectQuery<?> subSelect = dslContext.selectQuery(aggregateTable);

      // assemble all string joins and for each string join: create a sub select query
      ObjectSelectContext lateralJoinContext = new ObjectSelectContext( objectSelectContext.getObjectQueryContext() );

      addAggregateField(aggregateObjectFieldConfiguration, lateralJoinContext, subSelect, aggregateTable.getName());

      // add join condition to subselect query
      Condition condition = getJoinCondition( (PostgresFieldConfiguration) aggregateObjectFieldConfiguration.getField(), fieldTable, aggregateTypeConfiguration, aggregateTable );
      subSelect.addConditions(condition);

      // join with query
      var lateralTable = subSelect.asTable(objectSelectContext.newTableAlias());
      query.addSelect(lateralTable.asterisk());
      query.addJoin(lateralTable, JoinType.OUTER_APPLY);
      objectSelectContext.getAssembleFns()
          .put(aggregateObjectFieldConfiguration.getField()
                  .getName(),
              createMapAssembler(lateralJoinContext.getAssembleFns(), lateralJoinContext.getCheckNullAlias(),
                  false)::apply);
    });

  }

  private void addAggregateField(AggregateObjectFieldConfiguration aggregateObjectFieldConfiguration, ObjectSelectContext objectSelectContext, SelectQuery<?> query, String table) {
    aggregateObjectFieldConfiguration.getAggregateFields().forEach(aggregateFieldConfiguration -> {

      String columnAlias = objectSelectContext.newSelectAlias();
      String columnName = ((PostgresFieldConfiguration) aggregateFieldConfiguration.getField()).getColumn();

      Field<?> column = aggregateFieldFactory.create(aggregateFieldConfiguration, table, columnName, columnAlias).as(columnAlias);

      objectSelectContext.getAssembleFns()
          .put(aggregateFieldConfiguration.getField().getName(), row -> row.get(column.getName()));

     query.addSelect(column);
    });
  }

  private void addKeyCriteria(SelectQuery<?> query, List<KeyCriteria> keyCriteria, Table<?> fromTable) {

    /**
     * Condition condition = keyCriteria .forEach(keyCondition -> {
     *
     * var column = fromTable.field(keyCondition.getField(), String.class); return
     * column.equals(DSL.inline(keyCondition.getValue())); }) .reduce(DSL.noCondition(),
     * Condition::and); query.addConditions(condition);
     **/
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
