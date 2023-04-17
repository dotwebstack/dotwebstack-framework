package org.dotwebstack.framework.backend.postgres;

import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import java.util.Map;
import org.dotwebstack.framework.backend.postgres.query.Query;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.datafetchers.KeyGroupedFlux;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class PostgresBackendLoader implements BackendLoader {

  private final PostgresClient postgresClient;

  public PostgresBackendLoader(PostgresClient postgresClient) {
    this.postgresClient = postgresClient;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(SingleObjectRequest objectRequest, RequestContext requestContext) {
    if (objectRequest.getObjectType()
        .isNested()) {
      return Mono.just(Map.of());
    }

    var query = new Query(objectRequest, requestContext);

    return postgresClient.fetch(query)
        .singleOrEmpty();
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest, RequestContext requestContext) {
    var query = new Query(collectionRequest, requestContext);

    return postgresClient.fetch(query).map(stringObjectMap -> stringObjectMap);
  }

  @Override
  public Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> batchLoadMany(
      CollectionBatchRequest collectionBatchRequest, RequestContext requestContext) {
    var query = new Query(collectionBatchRequest, requestContext);

    return postgresClient.fetch(query)
        .groupBy(row -> getNestedMap(row, Query.GROUP_KEY))
        .map(
            groupedFlux -> new KeyGroupedFlux(groupedFlux.key(), groupedFlux.filter(PostgresBackendLoader::rowExists)));
  }

  @Override
  public Flux<Tuple2<Map<String, Object>, Map<String, Object>>> batchLoadSingle(BatchRequest batchRequest,
      RequestContext requestContext) {
    var query = new Query(batchRequest, requestContext);

    return postgresClient.fetch(query)
        .map(row -> Tuples.of(getNestedMap(row, Query.GROUP_KEY), rowExists(row) ? row : BackendLoader.NILL_MAP));
  }

  private static boolean rowExists(Map<String, Object> row) {
    return !row.containsKey(Query.EXISTS_KEY) || getNestedMap(row, Query.EXISTS_KEY).size() > 0;
  }
}
