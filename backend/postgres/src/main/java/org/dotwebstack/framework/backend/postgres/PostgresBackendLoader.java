package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.spi.ConnectionFactory;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.query.Query;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class PostgresBackendLoader implements BackendLoader {

  private final ConnectionFactory connectionFactory;

  public PostgresBackendLoader(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest, RequestContext requestContext) {
    if (objectRequest.getObjectType()
        .isNested()) {
      return Mono.just(Map.of());
    }

    var query = new Query(objectRequest, requestContext);

    return Mono.from(connectionFactory.create())
        .flatMapMany(connection -> query.execute(connection)
            .doFinally(signalType -> Mono.from(connection.close())
                .subscribe()))
        .singleOrEmpty();
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext requestContext) {
    var query = new Query(collectionRequest, requestContext);

    return Mono.from(connectionFactory.create())
        .flatMapMany(connection -> query.execute(connection)
            .doFinally(signalType -> Mono.from(connection.close())
                .subscribe()));
  }

  @Override
  public Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext requestContext) {
    var query = new Query(collectionBatchRequest, requestContext);

    return Mono.from(connectionFactory.create())
        .flatMapMany(connection -> query.executeBatchMany(connection)
            .doFinally(signalType -> Mono.from(connection.close())
                .subscribe()));
  }

  @Override
  public Flux<Tuple2<Map<String, Object>, Map<String, Object>>> batchLoadSingle(BatchRequest batchRequest,
      RequestContext requestContext) {
    var query = new Query(batchRequest, requestContext);

    return Mono.from(connectionFactory.create())
        .flatMapMany(connection -> query.executeBatchSingle(connection)
            .doFinally(signalType -> Mono.from(connection.close())
                .subscribe()));
  }
}
