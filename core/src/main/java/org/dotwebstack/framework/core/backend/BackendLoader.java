package org.dotwebstack.framework.core.backend;

import java.util.Map;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BackendLoader {

  Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest);

  Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest);
}
