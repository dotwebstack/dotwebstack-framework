package org.dotwebstack.framework.backend.postgres;

import java.util.*;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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
  public boolean supports(AbstractTypeConfiguration<?> typeConfiguration) {
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
    QueryWithAliasMap queryWithAliasMap = queryBuilder.build(typeConfiguration, environment.getSelectedFields());

    return this.execute(queryWithAliasMap.getQuery())
        .fetch()
        .one();
    // .map(map -> updateKeys(fieldConfigurationMap, map));
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Set<Object> keys, LoadEnvironment environment) {
    return null;
    // return keys.flatMap(key -> loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) environment.getTypeConfiguration();

    QueryBuilder queryBuilder = new QueryBuilder(dotWebStackConfiguration, dslContext);
    QueryWithAliasMap queryWithAliasMap = queryBuilder.build(typeConfiguration, environment.getSelectedFields());

    return this.execute(queryWithAliasMap.getQuery())
        .fetch()
        .all()
        .map(map -> rowMapToGraphQlMap(map, queryWithAliasMap.getColumnAliasMap()));
  }

  public static <K, V> Map<V, K> inverseMap(Map<K, V> sourceMap) {
    return sourceMap.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a));
  }

  private Map<String, Object> rowMapToGraphQlMap(Map<String, Object> rowMap, Map<Object, Object> columnAliasMap) {
    Map<String, Object> result = new HashMap<>();

    for (Object fieldName : columnAliasMap.keySet()) {
      if (columnAliasMap.get(fieldName) instanceof Map) {
        Map<Object, Object> nestedAliasMap = (Map<Object, Object>) columnAliasMap.get(fieldName);
        Map<String, Object> nestedResult = rowMapToGraphQlMap(rowMap, nestedAliasMap);
        result.put(fieldName.toString(), nestedResult);
        continue;
      }

      result.put(fieldName.toString(), rowMap.get(columnAliasMap.get(fieldName)
          .toString()));
    }
    return result;
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }

}
