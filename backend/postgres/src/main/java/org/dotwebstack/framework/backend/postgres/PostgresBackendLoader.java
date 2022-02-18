package org.dotwebstack.framework.backend.postgres;

import java.util.Map;
import org.dotwebstack.framework.backend.postgres.query.Query;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class PostgresBackendLoader implements BackendLoader {

  private final DatabaseClient databaseClient;

  public PostgresBackendLoader(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest, RequestContext requestContext) {
    if (objectRequest.getObjectType()
        .isNested()) {
      return Mono.just(Map.of());
    }

    var query = new Query(objectRequest, requestContext);

    return query.execute(databaseClient)
        .singleOrEmpty();
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext requestContext) {
    var query = new Query(collectionRequest, requestContext);

    return query.execute(databaseClient);
  }

  @Override
  public Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext requestContext) {
    var query = new Query(collectionBatchRequest, requestContext);

    return query.executeBatchMany(databaseClient);
  }

  @Override
  public Flux<Tuple2<Map<String, Object>, Map<String, Object>>> batchLoadSingle(BatchRequest batchRequest,
      RequestContext requestContext) {
    var query = new Query(batchRequest, requestContext);

    return query.executeBatchSingle(databaseClient);
  }
}
