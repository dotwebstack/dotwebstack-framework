package org.dotwebstack.framework.backend.postgres.query.objectQuery;

import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.QueryHolder;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
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

    ObjectQueryContext objectQueryContext = new ObjectQueryContext();
    Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();
    SelectJoinStep<Record> query = buildQuery(assembleFns, objectQueryContext, objectQuery);

    return QueryHolder.builder()
        .query(query)
        .mapAssembler(createMapAssembler(assembleFns, objectQueryContext.getCheckNullAlias(), true))
        .build();
  }

  public SelectJoinStep<Record> buildQuery(Map<String, Function<Map<String, Object>, Object>> assembleFns,
      ObjectQueryContext objectQueryContext, ObjectQuery objectQuery) {

    Table<Record> fromTable = DSL.table(((PostgresTypeConfiguration) objectQuery.getTypeConfiguration()).getTable());

    List<SelectFieldOrAsterisk> selectColumns =
        addScalarFields(assembleFns, objectQueryContext, fromTable, objectQuery.getScalarFields());
    SelectJoinStep<Record> query = dslContext.select(selectColumns)
        .from(fromTable);

    return query;
  }

  private List<SelectFieldOrAsterisk> addScalarFields(Map<String, Function<Map<String, Object>, Object>> assembleFns,
      ObjectQueryContext objectQueryContext, Table<Record> fromTable, List<FieldConfiguration> scalarFields) {
    return scalarFields.stream()
        .map(scalarField -> {
          String columnAlias = objectQueryContext.newSelectAlias();
          Field<Object> column =
              DSL.field(DSL.name(fromTable.getName(), ((PostgresFieldConfiguration) scalarField).getColumn()))
                  .as(columnAlias);
          assembleFns.put(scalarField.getName(), row -> row.get(column.getName()));

          // TODO why set checkNullAlias

          return column;
        })
        .collect(Collectors.toList());
  }
}
