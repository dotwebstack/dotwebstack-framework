package org.dotwebstack.framework.backend.postgres;

import static org.dotwebstack.framework.core.helpers.MapHelper.toGraphQlMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.PostgresQueryBuilder;
import org.dotwebstack.framework.backend.postgres.query.PostgresQueryHolder;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.DataLoaderResult;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
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

  @Override
  public Mono<DataLoaderResult> loadSingle(Filter filter, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) environment.getTypeConfiguration();

    PostgresQueryBuilder queryBuilder = new PostgresQueryBuilder(dotWebStackConfiguration, dslContext);
    PostgresQueryHolder postgresQueryHolder = queryBuilder.build(typeConfiguration, environment, filter);

    return this.execute(postgresQueryHolder.getQuery())
        .fetch()
        .one()
        .map(map -> toGraphQlMap(map, postgresQueryHolder.getFieldAliasMap()))
        .map(map -> DataLoaderResult.builder()
            .data(map)
            .build());
  }

  @Override
  public Flux<Tuple2<Filter, DataLoaderResult>> batchLoadSingle(Set<Filter> filters, LoadEnvironment environment) {
    return Flux.fromIterable(filters)
        .flatMap(key -> loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<DataLoaderResult> loadMany(Filter filter, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) environment.getTypeConfiguration();

    PostgresQueryBuilder queryBuilder = new PostgresQueryBuilder(dotWebStackConfiguration, dslContext);
    PostgresQueryHolder postgresQueryHolder = queryBuilder.build(typeConfiguration, environment, filter);

    return this.execute(postgresQueryHolder.getQuery())
        .fetch()
        .all()
        .map(map -> toGraphQlMap(map, postgresQueryHolder.getFieldAliasMap()))
        .map(map -> DataLoaderResult.builder()
            .data(map)
            .build());
  }

  @Override
  public Flux<Flux<DataLoaderResult>> batchLoadMany(List<Filter> filters, LoadEnvironment environment) {
    return Flux.fromIterable(filters)
        .map(key -> this.loadMany(key, environment));
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
