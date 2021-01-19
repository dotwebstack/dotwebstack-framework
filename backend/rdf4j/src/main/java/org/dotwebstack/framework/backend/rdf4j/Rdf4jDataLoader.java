package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.MapHelper.toGraphQlMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.query.Rdf4jQueryBuilder;
import org.dotwebstack.framework.backend.rdf4j.query.Rdf4jQueryHolder;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class Rdf4jDataLoader implements BackendDataLoader {

  private static final Logger LOG = LoggerFactory.getLogger(Rdf4jDataLoader.class);

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
  public Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment) {
    Rdf4jQueryHolder queryHolder = getQueryHolder(key, environment);

    try (TupleQueryResult queryResult = executeQuery(queryHolder.getQuery())) {
      if (queryResult.hasNext()) {
        return Mono.just(toDataMap(queryResult.next()))
            .map(dataMap -> toGraphQlMap(dataMap, queryHolder.getFieldAliasMap()));
      }
      return Mono.empty();
    }
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Set<Object> keys, LoadEnvironment environment) {
    throw unsupportedOperationException("Not implemented yet!");
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
    Rdf4jQueryHolder queryHolder = getQueryHolder(key, environment);

    TupleQueryResult queryResult = executeQuery(queryHolder.getQuery());

    return Flux.fromIterable(queryResult.stream()
        .map(this::toDataMap)
        .map(dataMap -> toGraphQlMap(dataMap, queryHolder.getFieldAliasMap()))
        .collect(Collectors.toList()));
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }

  private TupleQueryResult executeQuery(String query) {
    return localRepositoryManager.getRepository("local")
        .getConnection()
        .prepareTupleQuery(query)
        .evaluate();
  }

  private Rdf4jQueryHolder getQueryHolder(Object key, LoadEnvironment environment) {
    Rdf4jTypeConfiguration typeConfiguration = (Rdf4jTypeConfiguration) environment.getTypeConfiguration();

    NodeShape nodeShape = nodeShapeRegistry.get(environment.getObjectType());

    return new Rdf4jQueryBuilder().build(typeConfiguration, nodeShape, environment, key);
  }

  private Map<String, Object> toDataMap(BindingSet bindingSet) {
    Map<String, Object> dataMap = new HashMap<>();
    bindingSet.getBindingNames()
        .forEach(bindingName -> {
          Value value = bindingSet.getValue(bindingName);

          Object objectValue;

          if (value instanceof IRI) {
            objectValue = ((IRI) value).getLocalName();
          } else {
            objectValue = value.stringValue();
          }
          dataMap.put(bindingName, objectValue);
        });

    return dataMap;
  }
}
