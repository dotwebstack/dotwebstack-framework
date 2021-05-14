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
    var objectQueryContext = new ObjectQueryContext();
    var query = buildQuery(objectQueryContext, objectQuery);
    var rowMapper = createMapAssembler(objectQueryContext.getAssembleFns(), objectQueryContext.getCheckNullAlias(),
        true);

    return databaseClient.sql(query.getSQL())
        .fetch()
        .all()
        .map(rowMapper);
  }

  public SelectQuery<?> buildQuery(ObjectQueryContext objectQueryContext, ObjectQuery objectQuery) {
    var fromTable =
        findTable(((PostgresTypeConfiguration) objectQuery.getTypeConfiguration()).getTable()).as(objectQueryContext.newTableAlias());

    SelectQuery<?> query = dslContext.selectQuery(fromTable);

    addScalarFields(objectQuery.getScalarFields(), objectQueryContext, query, fromTable);
    addObjectFields(objectQuery, objectQueryContext, query, fromTable);

    return query;
  }

  private void addObjectFields(ObjectQuery objectQuery, ObjectQueryContext objectQueryContext, SelectQuery<?> query,
      Table<?> table) {

    objectQuery.getObjectFields().forEach(objectField -> {

      SelectQuery<?> subSelect = buildQuery(objectQueryContext, objectField.getObjectQuery());

      Table<?> objectFieldTable =
          findTable(((PostgresTypeConfiguration) ((AbstractFieldConfiguration) objectField.getField()).getTypeConfiguration()).getTable());

      var leftColumn = table.field("postal_address", String.class);
      var rightColumn = objectFieldTable.field("identifier_address", String.class);
      // var leftColumn = DSL.name(table.getName(), "postal_address");
      // var rightColumn = DSL.name(objectFieldTable.getName(), "identifier_address");

      Condition joinCondition = leftColumn.eq(rightColumn);
      query.addJoin(subSelect, JoinType.OUTER_APPLY, joinCondition);
    });
  }

  private void addScalarFields(List<FieldConfiguration> scalarFields, ObjectQueryContext objectQueryContext,
      SelectQuery<?> query, Table<?> table) {

    scalarFields.forEach(scalarField -> {
      String columnAlias = objectQueryContext.newSelectAlias();
      Field<?> column = Objects.requireNonNull(table.field(scalarField.getName())).as(columnAlias);
      objectQueryContext.getAssembleFns()
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
