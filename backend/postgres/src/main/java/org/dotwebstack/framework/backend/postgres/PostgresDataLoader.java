package org.dotwebstack.framework.backend.postgres;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.QueryBuilder;
import org.dotwebstack.framework.backend.postgres.query.QueryHolder;
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

    QueryBuilder queryBuilder = new QueryBuilder(dotWebStackConfiguration, dslContext, environment);
    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, keyCondition);

    return this.execute(queryHolder.getQuery())
        .fetch()
        .one()
        .map(row -> queryHolder.getRowAssembler()
            .apply(row));
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

    QueryBuilder queryBuilder = new QueryBuilder(dotWebStackConfiguration, dslContext, environment);
    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, keyCondition);

    return this.execute(queryHolder.getQuery())
        .fetch()
        .all()
        .map(row -> queryHolder.getRowAssembler()
            .apply(row));
  }

  @Override
  public Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadMany(final Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);
    QueryBuilder queryBuilder = new QueryBuilder(dotWebStackConfiguration, dslContext, environment);
    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, keyConditions);

    return this.execute(queryHolder.getQuery())
        .fetch()
        .all()
        .groupBy(row -> getKeyConditionByKey(keyConditions, row, queryHolder.getKeyColumnNames()),
            row -> queryHolder.getRowAssembler()
                .apply(row));
  }

  private KeyCondition getKeyConditionByKey(Set<KeyCondition> keyConditions, Map<String, Object> row,
      Map<String, String> keyColumnNames) {
    return keyConditions.stream()
        .map(ColumnKeyCondition.class::cast)
        .filter(keyCondition -> {
          for (Entry<String, Object> valueEntry : keyCondition.getValueMap()
              .entrySet()) {
            String columnAlias = keyColumnNames.get(valueEntry.getKey());

            if (valueEntry.getValue()
                .equals(row.get(columnAlias))) {
              return true;
            }
          }

          return false;
        })
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find keyCondition."));
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
