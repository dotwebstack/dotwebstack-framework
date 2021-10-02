package org.dotwebstack.framework.backend.rdf4j;

import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.query.Query;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.eclipse.rdf4j.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Rdf4jBackendLoader implements BackendLoader {

  private final Repository repository;

  private final NodeShape nodeShape;

  public Rdf4jBackendLoader(Repository repository, NodeShape nodeShape) {
    this.repository = repository;
    this.nodeShape = nodeShape;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest) {
    var query = new Query(objectRequest, nodeShape);
    var connection = repository.getConnection();

    return query.execute(connection)
        .singleOrEmpty()
        .doFinally(signalType -> connection.close());
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest) {
    var query = new Query(collectionRequest, nodeShape);
    var connection = repository.getConnection();

    return query.execute(connection)
        .doFinally(signalType -> connection.close());
  }
}
