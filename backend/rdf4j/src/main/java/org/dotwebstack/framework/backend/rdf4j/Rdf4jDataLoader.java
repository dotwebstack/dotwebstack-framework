package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.MapHelper.toGraphQlMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.query.Rdf4jQueryBuilder;
import org.dotwebstack.framework.backend.rdf4j.query.Rdf4jQueryHolder;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.DataLoaderResult;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class Rdf4jDataLoader implements BackendDataLoader {

  private final LocalRepositoryManager localRepositoryManager;

  private final NodeShapeRegistry nodeShapeRegistry;

  public Rdf4jDataLoader(@NonNull LocalRepositoryManager localRepositoryManager,
      @NonNull NodeShapeRegistry nodeShapeRegistry) {
    this.localRepositoryManager = localRepositoryManager;
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof Rdf4jTypeConfiguration;
  }

  @Override
  public Mono<DataLoaderResult> loadSingle(Filter filter, LoadEnvironment environment) {
    Rdf4jQueryHolder queryHolder = getQueryHolder(filter, environment);

    try (TupleQueryResult queryResult = executeQuery(queryHolder.getQuery())) {
      if (queryResult.hasNext()) {
        return Mono.just(toDataMap(queryResult.next()))
            .map(dataMap -> toGraphQlMap(dataMap, queryHolder.getFieldAliasMap()))
            .map(map -> DataLoaderResult.builder()
                .data(map)
                .build());
      }
      return Mono.empty();
    }
  }

  @Override
  public Flux<Tuple2<Filter, DataLoaderResult>> batchLoadSingle(Set<Filter> filters, LoadEnvironment environment) {
    throw unsupportedOperationException("Not implemented yet!");
  }

  @Override
  public Flux<DataLoaderResult> loadMany(Filter filter, LoadEnvironment environment) {
    Rdf4jQueryHolder queryHolder = getQueryHolder(filter, environment);

    TupleQueryResult queryResult = executeQuery(queryHolder.getQuery());

    return Flux.fromStream(queryResult.stream()
        .map(this::toDataMap)
        .map(dataMap -> toGraphQlMap(dataMap, queryHolder.getFieldAliasMap()))
        .map(map -> DataLoaderResult.builder()
            .data(map)
            .build()));
  }

  @Override
  public Flux<GroupedFlux<Filter, DataLoaderResult>> batchLoadMany(Set<Filter> filters, LoadEnvironment environment) {
    throw unsupportedOperationException("Not implemented yet!");
  }

  private TupleQueryResult executeQuery(String query) {
    return localRepositoryManager.getRepository("local")
        .getConnection()
        .prepareTupleQuery(query)
        .evaluate();
  }

  private Rdf4jQueryHolder getQueryHolder(Filter filter, LoadEnvironment environment) {
    Rdf4jTypeConfiguration typeConfiguration = (Rdf4jTypeConfiguration) environment.getTypeConfiguration();

    NodeShape nodeShape = nodeShapeRegistry.get(environment.getObjectType());

    return new Rdf4jQueryBuilder().build(typeConfiguration, nodeShape, environment, filter);
  }

  private Map<String, Object> toDataMap(BindingSet bindingSet) {
    Map<String, Object> dataMap = new HashMap<>();
    bindingSet.getBindingNames()
        .forEach(bindingName -> {
          Value value = bindingSet.getValue(bindingName);
          dataMap.put(bindingName, value.stringValue());
        });

    return dataMap;
  }
}
