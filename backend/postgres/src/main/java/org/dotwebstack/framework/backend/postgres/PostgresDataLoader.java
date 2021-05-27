package org.dotwebstack.framework.backend.postgres;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.objectquery.ObjectQueryBuilder;
import org.dotwebstack.framework.backend.postgres.query.objectquery.ObjectSelectContext;
import org.dotwebstack.framework.backend.postgres.query.objectquery.PostgresKeyCriteria;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.query.model.CollectionQuery;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.jooq.Param;
import org.jooq.Query;
import org.jooq.SelectQuery;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class PostgresDataLoader implements BackendDataLoader {
  private static final String UNSUPPORTED_MESSAGE = "This is old implementation";

  private static final Logger LOG = LoggerFactory.getLogger(PostgresDataLoader.class);

  private final DatabaseClient databaseClient;

  private final ObjectQueryBuilder objectQueryBuilder;

  public PostgresDataLoader(DatabaseClient databaseClient, ObjectQueryBuilder objectQueryBuilder) {
    this.databaseClient = databaseClient;
    this.objectQueryBuilder = objectQueryBuilder;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof PostgresTypeConfiguration;
  }

  @Override
  public Mono<Map<String, Object>> loadSingleObject(ObjectQuery objectQuery) {
    var selectQueryBuilderResult = objectQueryBuilder.build(objectQuery, new ObjectSelectContext());

    return fetch(selectQueryBuilderResult.getQuery(), selectQueryBuilderResult.getMapAssembler()).single();
  }

  @Override
  public Flux<Map<String, Object>> loadManyObject(CollectionQuery collectionQuery) {
    var selectQueryBuilderResult = objectQueryBuilder.build(collectionQuery, new ObjectSelectContext());

    return fetch(selectQueryBuilderResult.getQuery(), selectQueryBuilderResult.getMapAssembler());
  }

  @Override
  public Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadManyObject(Set<KeyCondition> keyConditions,
      CollectionQuery collectionQuery) {
    collectionQuery.getObjectQuery()
        .getKeyCriteria()
        .addAll(keyConditions.stream()
            .map(ColumnKeyCondition.class::cast)
            .filter(keyCriteria -> keyCriteria.getJoinTable() == null)
            .map(key -> PostgresKeyCriteria.builder()
                .values(key.getValueMap())
                .build())
            .collect(Collectors.toList()));

    List<PostgresKeyCriteria> joinCriteria = keyConditions.stream()
        .map(ColumnKeyCondition.class::cast)
        .filter(keyCriteria -> keyCriteria.getJoinTable() != null)
        .map(key -> PostgresKeyCriteria.builder()
            .values(key.getValueMap())
            .joinTable(key.getJoinTable())
            .build())
        .collect(Collectors.toList());

    var selectQueryBuilderResult =
        objectQueryBuilder.build(collectionQuery, new ObjectSelectContext(joinCriteria, true));

    Map<String, String> keyColumnNames = selectQueryBuilderResult.getContext()
        .getKeyColumnNames();

    return this.execute(selectQueryBuilderResult.getQuery())
        .fetch()
        .all()
        .map(row -> row)
        .groupBy(row -> getKeyConditionByKey(keyConditions, row, keyColumnNames),
            row -> selectQueryBuilderResult.getMapAssembler()
                .apply(row));
  }

  private Flux<Map<String, Object>> fetch(SelectQuery<?> query, UnaryOperator<Map<String, Object>> mapAssembler) {
    String sql = query.getSQL(ParamType.INLINED);

    LOG.debug("Fetching with SQL: {}", sql);

    return databaseClient.sql(sql)
        .fetch()
        .all()
        .map(mapAssembler);
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(KeyCondition keyCondition, LoadEnvironment environment) {
    throw unsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Flux<Tuple2<KeyCondition, Map<String, Object>>> batchLoadSingle(Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    throw unsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Flux<Map<String, Object>> loadMany(KeyCondition keyCondition, LoadEnvironment environment) {
    throw unsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadMany(final Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    throw unsupportedOperationException(UNSUPPORTED_MESSAGE);
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
        .orElseThrow(() -> illegalStateException("Unable to find keyCondition."));
  }

  private DatabaseClient.GenericExecuteSpec execute(Query query) {
    String sql = query.getSQL(ParamType.NAMED);
    List<Param<?>> params = getParams(query);
    LOG.debug("PostgreSQL query: {}", sql);
    LOG.debug("Binding variables: {}", params);

    DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);

    for (var index = 0; index < params.size(); index++) {
      executeSpec = executeSpec.bind(index, Objects.requireNonNull(params.get(index)
          .getValue()));
    }

    return executeSpec;
  }

  private List<Param<?>> getParams(Query query) {
    return query.getParams()
        .values()
        .stream()
        .filter(Predicate.not(Param::isInline))
        .collect(Collectors.toList());
  }

  @Override
  public boolean useObjectQueryApproach() {
    return true;
  }
}
