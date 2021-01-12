package org.dotwebstack.framework.backend.postgres;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.keys.FieldKey;
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
    Query query = createQuery(key, environment);

    final Map<String, PostgresFieldConfiguration> fieldConfigurationMap =
        ((PostgresTypeConfiguration) environment.getTypeConfiguration()).getFields();

    return this.execute(query)
        .fetch()
        .one()
        .map(map -> updateKeys(fieldConfigurationMap, map));
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Set<Object> keys, LoadEnvironment environment) {
    return null;
    // return keys.flatMap(key -> loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
    Query query = createQuery(key, environment);

    final Map<String, PostgresFieldConfiguration> fieldConfigurationMap =
        ((PostgresTypeConfiguration) environment.getTypeConfiguration()).getFields();

    return this.execute(query)
        .fetch()
        .all()
        .map(map -> updateKeys(fieldConfigurationMap, map));
  }

  // Experimental code
  private Map<String, Object> updateKeys(Map<String, PostgresFieldConfiguration> fieldConfigurationMap,
      Map<String, Object> map) {
    map.keySet()
        .forEach(fieldName -> {
          fieldConfigurationMap.values()
              .stream()
              .filter(v -> v.getJoinColumns() != null)
              .flatMap(v -> v.getJoinColumns()
                  .stream())
              .filter(joinColumn -> Objects.equals(joinColumn.getName(), fieldName))
              .findFirst()
              .ifPresent(joinColumn -> map.put(fieldName, FieldKey.builder()
                  .name(joinColumn.getReferencedField())
                  .value(map.get(fieldName))
                  .build()));
        });
    return map;
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }

  private Query createQuery(Object key, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) environment.getTypeConfiguration();


    SelectJoinStep<Record> query = dslContext.select()
        .from(table(typeConfiguration.getTable()));

    if (key instanceof FieldKey) {
      FieldKey fieldKey = (FieldKey) key;
      query.where(field(fieldKey.getName()).eq(fieldKey.getValue()));
    }

    return query;
  }
}
