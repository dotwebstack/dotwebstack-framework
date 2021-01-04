package org.dotwebstack.framework.backend.postgres;

import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.dotwebstack.framework.backend.postgres.query.SchemaTable;
import org.dotwebstack.framework.backend.postgres.query.SelectQuery;
import org.dotwebstack.framework.backend.postgres.query.WhereCondition;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class PostgresDataLoader implements BackendDataLoader {

  private final TableRegistry tableRegistry;

  private final DatabaseClient databaseClient;

  public PostgresDataLoader(TableRegistry tableRegistry, DatabaseClient databaseClient) {
    this.tableRegistry = tableRegistry;
    this.databaseClient = databaseClient;
  }

  @Override
  public boolean supports(GraphQLObjectType objectType) {
    return tableRegistry.contains(objectType.getName());
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment) {
    return this.createQuery(key, environment)
        .execute(databaseClient)
        .fetch()
        .one();
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Flux<Object> keys,
      LoadEnvironment environment) {
    return keys.flatMap(key ->
        loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
    return this.createQuery(null, environment)
        .execute(databaseClient)
        .fetch()
        .all();
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }

  private SelectQuery createQuery(Object key, LoadEnvironment environment) {
    AtomicInteger tableCounter = new AtomicInteger();
    TableRegistry.TableMapping tableMapping = tableRegistry.get(environment.getObjectType().getName());

    SchemaTable fromTable = SchemaTable.builder()
        .name(tableMapping.getName())
        .alias("t".concat(String.valueOf(tableCounter.incrementAndGet())))
        .build();

    SelectQuery.SelectQueryBuilder queryBuilder = SelectQuery.builder()
        .fromTable(fromTable);

    if (key != null) {
      queryBuilder.whereCondition(
          WhereCondition.builder()
              .column(fromTable.newColumn(tableMapping.getKeyColumn()))
              .value(key)
              .build());
    }

    return queryBuilder.build();
  }
}
