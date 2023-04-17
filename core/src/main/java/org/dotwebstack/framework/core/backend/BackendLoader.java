package org.dotwebstack.framework.core.backend;

import java.util.Map;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendLoader {

  Map<String, Object> NILL_MAP = Map.of();

  Mono<Map<String, Object>> loadSingle(SingleObjectRequest objectRequest, RequestContext context);

  Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext context);

  Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext context);

  Flux<Tuple2<Map<String, Object>, Map<String, Object>>> batchLoadSingle(BatchRequest batchRequest,
      RequestContext requestContext);
}
