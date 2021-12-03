package org.dotwebstack.framework.core.backend;

import java.util.Map;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

public interface BackendLoader {

  Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest, RequestContext context);

  Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext context);

  Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext context);
}
