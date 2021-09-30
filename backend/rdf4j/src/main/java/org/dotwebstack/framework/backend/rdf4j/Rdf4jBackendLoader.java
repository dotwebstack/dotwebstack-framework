package org.dotwebstack.framework.backend.rdf4j;

import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jObjectType;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Rdf4jBackendLoader implements BackendLoader {

  private final Rdf4jObjectType objectType;

  public Rdf4jBackendLoader(Rdf4jObjectType objectType) {
    this.objectType = objectType;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest) {
    return Mono.empty();
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest) {
    return Flux.empty();
  }
}
