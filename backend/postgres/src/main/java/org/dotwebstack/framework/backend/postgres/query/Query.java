package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.RowMapper;
import org.dotwebstack.framework.core.datafetchers.KeyGroupedFlux;
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

  @SuppressWarnings("unchecked")
  public Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> executeBatch(DatabaseClient databaseClient) {
    return execute(databaseClient).groupBy(row -> (Map<String, Object>) row.get(GROUP_KEY))
        .map(groupedFlux -> new KeyGroupedFlux(groupedFlux.key(),
            groupedFlux.filter(row -> !row.containsKey(EXISTS_KEY) || getNestedMap(row, EXISTS_KEY).size() > 0)));
  }

  private List<Param<?>> getParams(SelectQuery<Record> selectQuery) {
    return selectQuery.getParams()
        .values()
        .stream()
        .filter(Predicate.not(Param::isInline))
        .collect(Collectors.toList());
  }

  private SelectQuery<Record> createSelect(CollectionRequest collectionRequest) {
    return SelectBuilder.newSelect()
        .requestContext(requestContext)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .build(collectionRequest, null);
  }

  private SelectQuery<Record> createSelect(CollectionBatchRequest collectionBatchRequest) {
    var collectionRequest = collectionBatchRequest.getCollectionRequest();

    return SelectBuilder.newSelect()
        .requestContext(requestContext)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .build(collectionRequest, collectionBatchRequest.getJoinCriteria());
  }

  private SelectQuery<Record> createSelect(ObjectRequest objectRequest) {
    return SelectBuilder.newSelect()
        .requestContext(requestContext)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .build(objectRequest);
  }
}
