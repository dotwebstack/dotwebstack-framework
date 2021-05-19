package org.dotwebstack.framework.backend.postgres;

import static org.dotwebstack.framework.backend.postgres.query.Page.pageWithDefaultSize;
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
import org.dotwebstack.framework.backend.postgres.query.QueryParameters;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilderResult;
import org.dotwebstack.framework.backend.postgres.query.objectquery.ObjectQueryBuilder;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
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

  private static final Logger LOG = LoggerFactory.getLogger(PostgresDataLoader.class);

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DatabaseClient databaseClient;

  private final org.dotwebstack.framework.backend.postgres.query.QueryBuilder queryBuilder;

  private final ObjectQueryBuilder objectQueryBuilder;

  public PostgresDataLoader(DotWebStackConfiguration dotWebStackConfiguration, DatabaseClient databaseClient,
      org.dotwebstack.framework.backend.postgres.query.QueryBuilder queryBuilder,
      ObjectQueryBuilder objectQueryBuilder) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.queryBuilder = queryBuilder;
    this.databaseClient = databaseClient;
    this.objectQueryBuilder = objectQueryBuilder;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof PostgresTypeConfiguration;
  }

  // TODO: uitwerken
  @Override
  public Mono<Map<String, Object>> loadSingleObject(ObjectQuery objectQuery) {
    SelectQueryBuilderResult selectQueryBuilderResult = objectQueryBuilder.build(objectQuery);

    return fetch(selectQueryBuilderResult.getQuery(), selectQueryBuilderResult.getMapAssembler()).single();
  }

  // TODO: uitwerken
  @Override
  public Flux<Map<String, Object>> loadManyObject(CollectionQuery collectionQuery) {
    SelectQueryBuilderResult selectQueryBuilderResult = objectQueryBuilder.build(collectionQuery);

    return fetch(selectQueryBuilderResult.getQuery(), selectQueryBuilderResult.getMapAssembler());
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
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    var queryParameters = QueryParameters.builder()
        .selectionSet(environment.getSelectionSet())
        .keyConditions(keyCondition != null ? List.of(keyCondition) : List.of())
        .build();

    var queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

    return this.execute(queryHolder.getQuery())
        .fetch()
        .one()
        .map(row -> queryHolder.getMapAssembler()
            .apply(row));
  }

  @Override
  public Flux<Tuple2<KeyCondition, Map<String, Object>>> batchLoadSingle(Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    throw unsupportedOperationException("Batch load single is not supported!");
  }

  @Override
  public Flux<Map<String, Object>> loadMany(KeyCondition keyCondition, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    var queryParametersBuilder = QueryParameters.builder()
        .selectionSet(environment.getSelectionSet())
        .keyConditions(keyCondition != null ? List.of(keyCondition) : List.of());

    if (!environment.isSubscription()) {
      queryParametersBuilder.page(pageWithDefaultSize());
    }

    var queryHolder = queryBuilder.build(typeConfiguration, queryParametersBuilder.build());

    return this.execute(queryHolder.getQuery())
        .fetch()
        .all()
        .map(row -> queryHolder.getMapAssembler()
            .apply(row));
  }

  @Override
  public Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadMany(final Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    var queryParameters = QueryParameters.builder()
        .selectionSet(environment.getSelectionSet())
        .keyConditions(keyConditions)
        .build();

    var queryHolder = queryBuilder.build(typeConfiguration, queryParameters, true);

    return this.execute(queryHolder.getQuery())
        .fetch()
        .all()
        .groupBy(row -> getKeyConditionByKey(keyConditions, row, queryHolder.getKeyColumnNames()),
            row -> queryHolder.getMapAssembler()
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
