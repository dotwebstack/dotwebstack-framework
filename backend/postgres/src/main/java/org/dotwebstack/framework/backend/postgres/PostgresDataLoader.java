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
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.QueryBuilder;
import org.dotwebstack.framework.backend.postgres.query.QueryHolder;
import org.dotwebstack.framework.backend.postgres.query.QueryParameters;
import org.dotwebstack.framework.core.config.ContextArgumentConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
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

@Component
public class PostgresDataLoader implements BackendDataLoader {

  private static final Logger LOG = LoggerFactory.getLogger(PostgresDataLoader.class);

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DatabaseClient databaseClient;

  private final QueryBuilder queryBuilder;

  public PostgresDataLoader(DotWebStackConfiguration dotWebStackConfiguration, DatabaseClient databaseClient,
      QueryBuilder queryBuilder) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.databaseClient = databaseClient;
    this.queryBuilder = queryBuilder;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof PostgresTypeConfiguration;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(KeyCondition keyCondition, LoadEnvironment environment) {
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(environment.getSelectionSet())
        .keyConditions(keyCondition != null ? List.of(keyCondition) : List.of())
        .filters(getFilters(environment))
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters);

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

    QueryParameters.QueryParametersBuilder queryParametersBuilder = QueryParameters.builder()
        .selectionSet(environment.getSelectionSet())
        .keyConditions(keyCondition != null ? List.of(keyCondition) : List.of())
        .filters(getFilters(environment));

    if (!environment.isSubscription()) {
      queryParametersBuilder.page(pageWithDefaultSize());
    }

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParametersBuilder.build());

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

    QueryParameters queryParameters = QueryParameters.builder()
        .selectionSet(environment.getSelectionSet())
        .keyConditions(keyConditions)
        .filters(getFilters(environment))
        .build();

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, queryParameters, true);

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

    for (int index = 0; index < params.size(); index++) {
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

  private List<String> getFilters(LoadEnvironment environment) {
    Map<String, ContextArgumentConfiguration> contextArguments = dotWebStackConfiguration.getContextArguments();
    return environment.getExecutionStepInfo()
        .getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> contextArguments.containsKey(argument.getName()))
        .map(argument -> getFilter(environment, contextArguments.get(argument.getName()), argument))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private String getFilter(LoadEnvironment environment, ContextArgumentConfiguration contextArgumentConfiguration,
      graphql.schema.GraphQLArgument argument) {
    Object value = environment.getExecutionStepInfo()
        .getArguments()
        .get(argument.getName());
    String filterExpr = contextArgumentConfiguration.getFilterExpr();

    switch (contextArgumentConfiguration.getType()) {
      case DATE:
        // TODO filter.replace $var to_date(value, 'MM/DD/YYYY')
        throw new UnsupportedOperationException("Date not implemented yet!");
      case DATETIME:
        // TODO filter.replace $var to_timestamp(value, 'MM/DD/YYYY HH24:MI:SS')
        throw new UnsupportedOperationException("DateTime not implemented yet!");
      case STRING:
        return filterExpr.replace("$val", "'".concat((String) value)
            .concat("'"));
      case BOOLEAN:
        boolean useFilter = (boolean) value;
        if (useFilter) {
          return filterExpr;
        }
        break;
      case INT:
      case FLOAT:
        return filterExpr.replace("$val", value.toString());
      default:
        throw new IllegalStateException(String.format("Unknown type %s.", contextArgumentConfiguration.getType()));
    }

    return null;
  }
}
