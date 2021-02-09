package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.query.QueryBuilder;
import org.dotwebstack.framework.backend.rdf4j.query.QueryHolder;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
@Slf4j
public class Rdf4jDataLoader implements BackendDataLoader {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final QueryBuilder queryBuilder;

  private final LocalRepositoryManager localRepositoryManager;

  public Rdf4jDataLoader(@NonNull DotWebStackConfiguration dotWebStackConfiguration, @NonNull QueryBuilder queryBuilder,
      @NonNull LocalRepositoryManager localRepositoryManager) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.queryBuilder = queryBuilder;
    this.localRepositoryManager = localRepositoryManager;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof Rdf4jTypeConfiguration;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(KeyCondition keyCondition, LoadEnvironment environment) {
    Rdf4jTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, environment.getSelectionSet(), keyCondition);

    try (TupleQueryResult queryResult = executeQuery(queryHolder.getQuery())) {
      if (queryResult.hasNext()) {
        return Mono.just(queryResult.next())
            .map(bindingSet -> queryHolder.getMapAssembler()
                .apply(bindingSet));
      }
      return Mono.empty();
    }
  }

  @Override
  public Flux<Tuple2<KeyCondition, Map<String, Object>>> batchLoadSingle(Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    throw unsupportedOperationException("Not implemented yet!");
  }

  @Override
  public Flux<Map<String, Object>> loadMany(KeyCondition keyConditions, LoadEnvironment environment) {
    Rdf4jTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, environment.getSelectionSet(), keyConditions);

    TupleQueryResult queryResult = executeQuery(queryHolder.getQuery());

    return Flux.fromStream(queryResult.stream())
        .map(bindingSet -> queryHolder.getMapAssembler()
            .apply(bindingSet));
  }

  @Override
  public Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadMany(Set<KeyCondition> keyConditions,
      LoadEnvironment environment) {
    throw unsupportedOperationException("Not supported yet!");
  }

  private TupleQueryResult executeQuery(String query) {

    LOG.debug("Sparql query: {}", query);

    return localRepositoryManager.getRepository("local")
        .getConnection()
        .prepareTupleQuery(query)
        .evaluate();
  }
}
