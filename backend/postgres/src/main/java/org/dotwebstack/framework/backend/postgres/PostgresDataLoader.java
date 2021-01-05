package org.dotwebstack.framework.backend.postgres;

import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.jooq.DSLContext;
import org.jooq.Param;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class PostgresDataLoader implements BackendDataLoader {

  private static final Logger LOG = LoggerFactory.getLogger(PostgresDataLoader.class);

  private final TableRegistry tableRegistry;

  private final DatabaseClient databaseClient;

  private final DSLContext dslContext;

  public PostgresDataLoader(TableRegistry tableRegistry, DatabaseClient databaseClient, DSLContext dslContext) {
    this.tableRegistry = tableRegistry;
    this.databaseClient = databaseClient;
    this.dslContext = dslContext;
  }

  @Override
  public boolean supports(GraphQLObjectType objectType) {
    return tableRegistry.contains(objectType.getName());
  }

  private DatabaseClient.GenericExecuteSpec execute(Query query) {
    String sql = query.getSQL(ParamType.NAMED);
    Map<String, Param<?>> params = query.getParams();

    LOG.debug("PostgreSQL query: {}", sql);
    LOG.debug("Binding variables: {}", params);

    DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);

    for (Map.Entry<String, Param<?>> param : params.entrySet()) {
      executeSpec = executeSpec.bind(param.getKey(),
          Objects.requireNonNull(param.getValue().getValue()));
    }

    return executeSpec;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment) {
    Query query = createQuery(key, environment);

    return this.execute(query)
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
    Query query = createQuery(key, environment);

    return this.execute(query)
        .fetch()
        .all();
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }

  private Query createQuery(Object key, LoadEnvironment environment) {
    TableRegistry.TableMapping tableMapping = tableRegistry.get(
        environment.getObjectType().getName());

    SelectJoinStep<Record> query = dslContext.select()
        .from(tableMapping.getTable());

    if (key != null) {
      query.where(tableMapping.getKeyColumn().eq(key));
    }

    return query;
  }
}
