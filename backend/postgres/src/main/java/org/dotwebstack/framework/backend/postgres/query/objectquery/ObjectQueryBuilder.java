package org.dotwebstack.framework.backend.postgres.query.objectquery;

import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ObjectQueryBuilder {

  private final DSLContext dslContext;

  private final DatabaseClient databaseClient;

  public ObjectQueryBuilder(DSLContext dslContext, DatabaseClient databaseClient) {
    this.dslContext = dslContext;
    this.databaseClient = databaseClient;
  }

  public Flux<Map<String, Object>> build(ObjectQuery objectQuery) {

    var objectSelectContext = new ObjectSelectContext( new ObjectQueryContext());
    var query = buildQuery(objectSelectContext, objectQuery);
    var rowMapper = createMapAssembler(objectSelectContext.getAssembleFns(), objectSelectContext.getObjectQueryContext().getCheckNullAlias(),
        true);

    return databaseClient.sql(query.getSQL())
        .fetch()
        .all()
        .map(rowMapper);
  }

  public SelectQuery<?> buildQuery(ObjectSelectContext objectSelectContext, ObjectQuery objectQuery) {
    var fromTable =
        findTable(((PostgresTypeConfiguration) objectQuery.getTypeConfiguration()).getTable()).as(objectSelectContext.newTableAlias());

    return buildQuery(objectSelectContext, objectQuery, fromTable) ;
  }

  public SelectQuery<?> buildQuery(ObjectSelectContext objectSelectContext, ObjectQuery objectQuery, Table<?> fromTable) {

    SelectQuery<?> query = dslContext.selectQuery(fromTable);

    addScalarFields(objectQuery.getScalarFields(), objectSelectContext, query, fromTable);
    addObjectFields(objectQuery, objectSelectContext, query, fromTable);

    return query;
  }

  private void addObjectFields(ObjectQuery objectQuery, ObjectSelectContext objectSelectContext, SelectQuery<?> query,
      Table<?> table) {

    objectQuery.getObjectFields().forEach(objectField -> {

      Table<?> objectFieldTable =
              findTable(((PostgresTypeConfiguration) ((AbstractFieldConfiguration) objectField.getField()).getTypeConfiguration()).getTable()).asTable(objectSelectContext.newTableAlias());

      ObjectSelectContext lateralJoinContext = new ObjectSelectContext( objectSelectContext.getObjectQueryContext() );
      Table<?> subSelect = buildQuery(lateralJoinContext, objectField.getObjectQuery(), objectFieldTable).asTable(objectSelectContext.newTableAlias());

      // var leftColumn = table.field("postal_address", String.class);
      // var rightColumn = subSelect.field("identifier_address", String.class);
      var leftColumn = DSL.field(DSL.name(table.getName(), "postal_address"));
      var rightColumn = DSL.field(DSL.name(subSelect.getName(), "identifier_address"));

      Condition joinCondition = leftColumn.eq(rightColumn);

      subSelect = subSelect.where( joinCondition);

      query.addJoin(subSelect, JoinType.OUTER_APPLY);  // TODO join condition is incorrect for some reason
      objectSelectContext.getAssembleFns().put( objectField.getField().getName(), createMapAssembler(lateralJoinContext.getAssembleFns(), lateralJoinContext.getObjectQueryContext().getCheckNullAlias(), true)::apply);
    });
  }

  private void addScalarFields(List<FieldConfiguration> scalarFields, ObjectSelectContext objectSelectContext,
      SelectQuery<?> query, Table<?> table) {

    scalarFields.forEach(scalarField -> {
      String columnAlias = objectSelectContext.newSelectAlias();
      Field<?> column = Objects.requireNonNull(table.field(scalarField.getName())).as(columnAlias);
      objectSelectContext.getAssembleFns()
          .put(scalarField.getName(), row -> row.get(column.getName()));

      // TODO why set checkNullAlias

      query.addSelect(column);
    });
  }

  private Table<?> findTable(String name) {

    // TODO add to PostgresTypeConfig
    String[] path = name.split("\\.");
    var tables = dslContext.meta().getTables(path[path.length - 1]);

    return tables.get(0);
  }
}
