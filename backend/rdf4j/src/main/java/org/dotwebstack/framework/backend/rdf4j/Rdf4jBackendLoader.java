package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.query.Query;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.query.model.*;
import org.eclipse.rdf4j.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class Rdf4jBackendLoader implements BackendLoader {

  private final Repository repository;

  private final NodeShape nodeShape;

  public Rdf4jBackendLoader(Repository repository, NodeShape nodeShape) {
    this.repository = repository;
    this.nodeShape = nodeShape;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest, RequestContext requestContext) {
    var query = new Query(objectRequest, nodeShape);
    var connection = repository.getConnection();

    return query.execute(connection)
        .singleOrEmpty()
        .doFinally(signalType -> connection.close());
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext requestContext) {
    var query = new Query(collectionRequest, nodeShape);
    var connection = repository.getConnection();

    return query.execute(connection)
        .doFinally(signalType -> connection.close());
  }

  @Override
  public Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext context) {
    throw unsupportedOperationException("Not supported yet");
  }

  @Override
  public Flux<Tuple2<Map<String, Object>, Map<String, Object>>> batchLoadSingle(BatchRequest batchRequest, RequestContext requestContext) {
    return null;
  }
}
