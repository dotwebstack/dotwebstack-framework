package org.dotwebstack.framework.backend.postgres;

import java.util.Map;
import org.dotwebstack.framework.backend.postgres.query.Query;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.DSLContext;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PostgresBackendLoader implements BackendLoader {

  private final DatabaseClient databaseClient;

  private final DSLContext dslContext;

  public PostgresBackendLoader(DatabaseClient databaseClient, DSLContext dslContext) {
    this.databaseClient = databaseClient;
    this.dslContext = dslContext;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(ObjectRequest objectRequest) {
    var query = new Query(objectRequest, dslContext);

    return query.execute(databaseClient)
        .singleOrEmpty();
  }

  @Override
  public Flux<Map<String, Object>> loadMany(CollectionRequest collectionRequest) {
    var query = new Query(collectionRequest, dslContext);
    return query.execute(databaseClient);
  }
}
