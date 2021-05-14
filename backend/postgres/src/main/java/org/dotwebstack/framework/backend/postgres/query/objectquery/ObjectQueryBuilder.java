package org.dotwebstack.framework.backend.postgres.query.objectquery;

import java.util.Objects;
import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;

import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.QueryHolder;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class ObjectQueryBuilder {

  private final DSLContext dslContext;

  public ObjectQueryBuilder(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  public QueryHolder build(ObjectQuery objectQuery) {

    var objectQueryContext = new ObjectQueryContext();
    var query = buildQuery(objectQueryContext, objectQuery);

    return QueryHolder.builder()
        .query(query)
        .mapAssembler(
            createMapAssembler(objectQueryContext.getAssembleFns(), objectQueryContext.getCheckNullAlias(), true))
        .build();
  }

  public SelectQuery<?> buildQuery(ObjectQueryContext objectQueryContext, ObjectQuery objectQuery) {

    var fromTable = findTable(((PostgresTypeConfiguration) objectQuery.getTypeConfiguration()).getTable());
    SelectQuery<?> query = dslContext.selectQuery(fromTable);

    addScalarFields(objectQuery.getScalarFields(), objectQueryContext, query, fromTable);

    return query;
  }

  private List<SelectFieldOrAsterisk> addNestedObjectFields(ObjectFieldConfiguration nestedObject,
      Table<Record> fromTable, ObjectQueryContext objectQueryContext) {
    return List.of();
  }

  private void addScalarFields(List<FieldConfiguration> scalarFields, ObjectQueryContext objectQueryContext, SelectQuery<?> query, Table<?> table) {
    scalarFields.forEach(scalarField -> {
          String columnAlias = objectQueryContext.newSelectAlias();
          Field<?> column = Objects.requireNonNull( table.field(scalarField.getName()) ).as(columnAlias);
          objectQueryContext.getAssembleFns()
              .put(scalarField.getName(), row -> row.get(column.getName()));

          // TODO why set checkNullAlias

            query.addSelect(column);
        });
  }

  private Table<?> findTable( String name ){

    String [] path = name.split("\\." );
    var tables = dslContext.meta().getTables( path[ path.length - 1 ] );

    return tables.get( 0 );
  }
}
