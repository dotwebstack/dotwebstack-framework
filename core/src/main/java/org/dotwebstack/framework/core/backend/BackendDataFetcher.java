package org.dotwebstack.framework.core.backend;

import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isListType;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isSubscription;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.MappedBatchLoader;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.RequestContext;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

class BackendDataFetcher implements DataFetcher<Object> {

  private final BackendLoader backendLoader;

  private final BackendRequestFactory requestFactory;

  public BackendDataFetcher(BackendLoader backendLoader, BackendRequestFactory requestFactory) {
    this.backendLoader = backendLoader;
    this.requestFactory = requestFactory;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    Map<String, Object> source = environment.getSource();

    var fieldName = environment.getField()
        .getName();

    // Data was eager-loaded by parent
    if (source != null && source.containsKey(fieldName)) {
      return source.get(fieldName);
    }

    var isSubscription = isSubscription(environment.getOperationDefinition());
    var requestContext = requestFactory.createRequestContext(environment);

    if (isSubscription || isListType(environment.getFieldType())) {
      var collectionRequest = requestFactory.createCollectionRequest(environment);

      if (source != null) {
        var joinCondition = (JoinCondition) source.get(JOIN_KEY_PREFIX.concat(fieldName));

        return getOrCreateBatchLoader(environment, requestContext).load(joinCondition.getKey());
      }

      var result = backendLoader.loadMany(collectionRequest, requestContext);

      if (isSubscription) {
        return result;
      }

      return result.collectList()
          .toFuture();
    }

    var objectRequest = requestFactory.createObjectRequest(environment);

    return backendLoader.loadSingle(objectRequest, requestContext)
        .toFuture();
  }

  private DataLoader<Object, Object> getOrCreateBatchLoader(DataFetchingEnvironment environment,
      RequestContext requestContext) {
    // Create separate data loader for every unique path, since every path can have different arguments
    // or selection
    var dataLoaderKey = String.join("/", environment.getExecutionStepInfo()
        .getPath()
        .getKeysOnly());

    return environment.getDataLoaderRegistry()
        .computeIfAbsent(dataLoaderKey, key -> createBatchLoader(environment, requestContext));
  }

  private DataLoader<Map<String, Object>, List<Map<String, Object>>> createBatchLoader(
      DataFetchingEnvironment environment, RequestContext requestContext) {
    var collectionRequest = requestFactory.createCollectionRequest(environment);

    MappedBatchLoader<Map<String, Object>, List<Map<String, Object>>> batchLoader = keys -> {
      var collectionBatchRequest = CollectionBatchRequest.builder()
          .collectionRequest(collectionRequest)
          .joinCriteria(JoinCriteria.builder()
              .keys(keys)
              .build())
          .build();

      return backendLoader.batchLoadMany(collectionBatchRequest, requestContext)
          .flatMap(group -> group.collectList()
              .map(rows -> Tuples.of(group.key(), rows)))
          .collectMap(Tuple2::getT1, Tuple2::getT2)
          .toFuture();
    };

    return DataLoaderFactory.newMappedDataLoader(batchLoader);
  }
}
