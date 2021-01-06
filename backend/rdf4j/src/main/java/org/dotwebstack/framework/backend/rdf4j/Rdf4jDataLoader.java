package org.dotwebstack.framework.backend.rdf4j;

import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class Rdf4jDataLoader implements BackendDataLoader {

  private static final Logger LOG = LoggerFactory.getLogger(Rdf4jDataLoader.class);

  private final LocalRepositoryManager localRepositoryManager;

  private final NodeShapeRegistry nodeShapeRegistry;

  public Rdf4jDataLoader(@NonNull LocalRepositoryManager localRepositoryManager, @NonNull NodeShapeRegistry nodeShapeRegistry) {
    this.localRepositoryManager = localRepositoryManager;
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public boolean supports(GraphQLObjectType objectType) {
    return nodeShapeRegistry.contains(objectType);
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment) {
    String query = createQuery(key, environment);
    String repositoryId = "local";

    TupleQueryResult queryResult = localRepositoryManager.getRepository(repositoryId)
        .getConnection()
        .prepareTupleQuery(query)
        .evaluate();

    List result = queryResult.stream().collect(Collectors.toList());
    return null; // TODO Resultaat naar map
  }

  private String createQuery(Object key, LoadEnvironment environment) {
    NodeShape nodeShape = nodeShapeRegistry.get(
        environment.getObjectType());

    Map<String, PropertyShape> propertyShapes = nodeShape.getPropertyShapes();

    List<IRI> propertyIris = propertyShapes.values().stream().map(propertyShape -> ((PredicatePath) propertyShape.getPath()).getIri()).collect(Collectors.toList());

    // TODO Query opmaken

    return "SELECT ?subject ?predicate ?object \n";
  }

  @Override
  public Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Flux<Object> keys,
      LoadEnvironment environment) {
    return keys.flatMap(key ->
        loadSingle(key, environment).map(item -> Tuples.of(key, item)));
  }

  @Override
  public Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment) {
    loadSingle(key, environment);
    return null; // TODO Resultaat naar map
  }

  @Override
  public Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment) {
    return Flux.fromIterable(keys)
        .map(key -> this.loadMany(key, environment));
  }

}
