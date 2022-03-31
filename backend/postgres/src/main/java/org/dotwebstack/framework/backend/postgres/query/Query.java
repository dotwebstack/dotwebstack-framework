package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.SelectBuilder.newSelect;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.RowMapper;
import org.dotwebstack.framework.core.datafetchers.KeyGroupedFlux;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.jooq.Param;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class Query {

  private static final Logger LOG = LoggerFactory.getLogger(Query.class);

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

  public Flux<Map<String, Object>> execute(DatabaseClient databaseClient) {
    var queryString = selectQuery.getSQL(ParamType.NAMED);
    List<Param<?>> params = getParams(selectQuery);

    LOG.debug("Binding variables: {}", params);
    LOG.debug("Executing query: {}", queryString);

    DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryString);

    for (var index = 0; index < params.size(); index++) {
      executeSpec = executeSpec.bind(index, Objects.requireNonNull(params.get(index)
          .getValue()));
    }

    return executeSpec.fetch()
        .all()
        .map(rowMapper);
  }

  public Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> executeBatchMany(DatabaseClient databaseClient) {
    return execute(databaseClient).groupBy(row -> getNestedMap(row, GROUP_KEY))
        .map(groupedFlux -> new KeyGroupedFlux(groupedFlux.key(), groupedFlux.filter(this::rowExists)));
  }

  public Flux<Tuple2<Map<String, Object>, Map<String, Object>>> executeBatchSingle(DatabaseClient databaseClient) {
    return execute(databaseClient)
        .map(row -> Tuples.of(getNestedMap(row, GROUP_KEY), rowExists(row) ? row : BackendLoader.NILL_MAP));
  }

  private boolean rowExists(Map<String, Object> row) {
    return !row.containsKey(EXISTS_KEY) || getNestedMap(row, EXISTS_KEY).size() > 0;
  }

  private List<Param<?>> getParams(SelectQuery<Record> selectQuery) {
    return selectQuery.getParams()
        .values()
        .stream()
        .filter(Predicate.not(Param::isInline))
        .collect(Collectors.toList());
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
