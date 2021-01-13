package org.dotwebstack.framework.backend.postgres;

import static org.dotwebstack.framework.core.helpers.MapHelper.toGraphQlMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.jooq.DSLContext;
import org.jooq.Param;
import org.jooq.Query;
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

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DatabaseClient databaseClient;

  private final DSLContext dslContext;

  public PostgresDataLoader(DotWebStackConfiguration dotWebStackConfiguration, DatabaseClient databaseClient,
      DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.databaseClient = databaseClient;
    this.dslContext = dslContext;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof PostgresTypeConfiguration;
  }

  private DatabaseClient.GenericExecuteSpec execute(Query query) {
    String sql = query.getSQL(ParamType.NAMED);
    Map<String, Param<?>> params = query.getParams();

    LOG.debug("PostgreSQL query: {}", sql);
    LOG.debug("Binding variables: {}", params);

    DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);

    for (Map.Entry<String, Param<?>> param : params.entrySet()) {
      executeSpec = executeSpec.bind(param.getKey(), Objects.requireNonNull(param.getValue()
          .getValue()));
    }

    return executeSpec;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) environment.getTypeConfiguration();

    QueryBuilder queryBuilder = new QueryBuilder(dotWebStackConfiguration, dslContext);
    QueryResult queryResult = queryBuilder.build(typeConfiguration, environment.getSelectedFields(), key);

    return this.execute(queryResult.getQuery())
        .fetch()
        .one()
        .map(map -> toGraphQlMap(map, queryResult.getFieldAliasMap()));
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Set<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .flatMap(key -> loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) environment.getTypeConfiguration();

    QueryBuilder queryBuilder = new QueryBuilder(dotWebStackConfiguration, dslContext);
    QueryResult queryResult = queryBuilder.build(typeConfiguration, environment.getSelectedFields(), key);

    return this.execute(queryResult.getQuery())
        .fetch()
        .all()
        .map(map -> toGraphQlMap(map, queryResult.getFieldAliasMap()));
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }
}
