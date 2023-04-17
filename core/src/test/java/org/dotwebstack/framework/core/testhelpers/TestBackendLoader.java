package org.dotwebstack.framework.core.testhelpers;

import java.util.Map;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class TestBackendLoader implements BackendLoader {
  private final DatabaseClient databaseClient;

  public TestBackendLoader(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(SingleObjectRequest objectRequest, RequestContext requestContext) {
    return Mono.empty();
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext requestContext) {
    return Flux.empty();
  }

  @Override
  public Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext requestContext) {
    return Flux.empty();
  }

  @Override
  public Flux<Tuple2<Map<String, Object>, Map<String, Object>>> batchLoadSingle(BatchRequest batchRequest,
      RequestContext requestContext) {
    return null;
  }
}
