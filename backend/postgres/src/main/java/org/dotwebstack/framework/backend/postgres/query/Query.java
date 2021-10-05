package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.RowMapper;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;

public class Query {

  private static final Logger LOG = LoggerFactory.getLogger(Query.class);

  private final AliasManager aliasManager = new AliasManager();

  private final RowMapper<Map<String, Object>> rowMapper = new RowMapper<>();

  private final SelectQuery<Record> selectQuery;

  private final DSLContext dslContext;

  public Query(CollectionRequest collectionRequest, DSLContext dslContext) {
    this.dslContext = dslContext;
    selectQuery = createSelect(collectionRequest);
  }

  public Query(ObjectRequest objectRequest, DSLContext dslContext) {
    this.dslContext = dslContext;
    selectQuery = createSelect(objectRequest);
  }

  public Flux<Map<String, Object>> execute(DatabaseClient databaseClient) {
    var queryString = selectQuery.getSQL(ParamType.INLINED);

    LOG.debug("Executing query: {}", queryString);

    return databaseClient.sql(queryString)
        .fetch()
        .all()
        .map(rowMapper);
  }

  private SelectQuery<Record> createSelect(CollectionRequest collectionRequest) {
    return createSelect(collectionRequest.getObjectRequest());
  }

  private SelectQuery<Record> createSelect(ObjectRequest objectRequest) {
    return SelectBuilder.newSelect()
        .dslContext(dslContext)
        .objectRequest(objectRequest)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .build();
  }
}
