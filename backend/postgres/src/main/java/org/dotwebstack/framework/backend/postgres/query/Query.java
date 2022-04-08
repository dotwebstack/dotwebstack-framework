package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.SelectBuilder.newSelect;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.RowMapper;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.jooq.Record;
import org.jooq.SelectQuery;

public class Query {

  public static final String GROUP_KEY = "$group";

  public static final String EXISTS_KEY = "$exists";

  private final AliasManager aliasManager = new AliasManager();

  private final RowMapper<Map<String, Object>> rowMapper = new RowMapper<>();

  private final SelectQuery<Record> selectQuery;

  private final RequestContext requestContext;

  public Query(CollectionRequest collectionRequest, RequestContext requestContext) {
    this.requestContext = requestContext;
    selectQuery = createSelect(collectionRequest);
  }

  public Query(CollectionBatchRequest collectionBatchRequest, RequestContext requestContext) {
    this.requestContext = requestContext;
    selectQuery = createSelect(collectionBatchRequest);
  }

  public Query(ObjectRequest objectRequest, RequestContext requestContext) {
    this.requestContext = requestContext;
    selectQuery = createSelect(objectRequest);
  }

  public Query(BatchRequest batchRequest, RequestContext requestContext) {
    this.requestContext = requestContext;
    selectQuery = createSelect(batchRequest);
  }

  public SelectQuery<Record> getSelectQuery() {
    return selectQuery;
  }

  public RowMapper<Map<String, Object>> getRowMapper() {
    return rowMapper;
  }

  private SelectQuery<Record> createSelect(CollectionRequest collectionRequest) {
    return newSelect().requestContext(requestContext)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .tableAlias(aliasManager.newAlias())
        .build(collectionRequest, null);
  }

  private SelectQuery<Record> createSelect(CollectionBatchRequest collectionBatchRequest) {
    var collectionRequest = collectionBatchRequest.getCollectionRequest();

    return newSelect().requestContext(requestContext)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .tableAlias(aliasManager.newAlias())
        .build(collectionRequest, collectionBatchRequest.getJoinCriteria());
  }

  private SelectQuery<Record> createSelect(ObjectRequest objectRequest) {
    return newSelect().requestContext(requestContext)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .tableAlias(aliasManager.newAlias())
        .build(objectRequest);
  }

  private SelectQuery<Record> createSelect(BatchRequest batchRequest) {
    return newSelect().requestContext(requestContext)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .tableAlias(aliasManager.newAlias())
        .build(batchRequest);
  }
}
