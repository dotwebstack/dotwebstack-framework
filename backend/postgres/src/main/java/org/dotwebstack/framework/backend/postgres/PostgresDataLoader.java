package org.dotwebstack.framework.backend.postgres;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.MapHelper.toGraphQlMap;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.PostgresQueryBuilder;
import org.dotwebstack.framework.backend.postgres.query.PostgresQueryHolder;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
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
import reactor.core.publisher.GroupedFlux;
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

  @Override
  public Mono<Map<String, Object>> loadSingle(KeyCondition keyCondition, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    PostgresQueryBuilder queryBuilder = new PostgresQueryBuilder(dotWebStackConfiguration, dslContext);
    PostgresQueryHolder postgresQueryHolder = queryBuilder.build(typeConfiguration, environment, keyCondition);

    return this.execute(postgresQueryHolder.getQuery())
        .fetch()
        .one()
        .map(row -> toGraphQlMap(row, postgresQueryHolder.getFieldAliasMap()));
  }

  @Override
  public Flux<Tuple2<KeyCondition, Map<String, Object>>> batchLoadSingle(Set<KeyCondition> filters,
      LoadEnvironment environment) {
    return Flux.fromIterable(filters)
        .flatMap(key -> loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(KeyCondition keyCondition, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    PostgresQueryBuilder queryBuilder = new PostgresQueryBuilder(dotWebStackConfiguration, dslContext);
    PostgresQueryHolder postgresQueryHolder = queryBuilder.build(typeConfiguration, environment, keyCondition);

    return this.execute(postgresQueryHolder.getQuery())
        .fetch()
        .all()
        .map(map -> toGraphQlMap(map, postgresQueryHolder.getFieldAliasMap()));
  }

  @Override
  public Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadMany(final Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);
    PostgresQueryBuilder queryBuilder = new PostgresQueryBuilder(dotWebStackConfiguration, dslContext);
    PostgresQueryHolder postgresQueryHolder = queryBuilder.build(typeConfiguration, environment, keyConditions);

    return this.execute(postgresQueryHolder.getQuery())
        .fetch()
        .all()
        .groupBy(row -> getKeyConditionByKey(keyConditions, row),
            row -> toGraphQlMap(row, postgresQueryHolder.getFieldAliasMap()));
  }

  private KeyCondition getKeyConditionByKey(Set<KeyCondition> keyConditions, Map<String, Object> row) {
    return keyConditions.stream()
        .map(ColumnKeyCondition.class::cast)
        .filter(columnKeyCondition -> row.get(columnKeyCondition.getColumnValues()
            .keySet()
            .iterator()
            .next())
            .equals(columnKeyCondition.getColumnValues()
                .values()
                .iterator()
                .next()))
        .findFirst()
        .orElseThrow(() -> illegalStateException("Unable to find keyCondition"));
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
}
