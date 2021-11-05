package org.dotwebstack.framework.core.backend;

import java.util.Map;

import org.dotwebstack.framework.core.query.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendLoader {

  Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest, RequestContext context);

  Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext context);

  Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext context);

  Flux<Tuple2<Map<String,Object>, Map<String, Object>>> batchLoadSingle(BatchRequest batchRequest, RequestContext requestContext);
}
