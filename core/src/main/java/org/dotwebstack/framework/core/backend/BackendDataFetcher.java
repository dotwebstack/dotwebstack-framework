package org.dotwebstack.framework.core.backend;

import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isListType;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isSubscription;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    // TODO: getExecutionStepInfo verplaatsen naar aparte helper.
    var executionStepInfo = requestFactory.getExecutionStepInfo(environment);

    var fieldName = executionStepInfo.getField()
        .getName();

    // Data was eager-loaded by parent
    if (source != null && source.containsKey(fieldName)) {
      return source.get(fieldName);
    }

    var isSubscription = isSubscription(environment.getOperationDefinition());
    var requestContext = requestFactory.createRequestContext(environment);

    if (isSubscription || isListType(environment.getFieldType())) {
      var collectionRequest = requestFactory.createCollectionRequest(executionStepInfo, environment.getSelectionSet());

      if (source != null) {
        var completableFutures = source.entrySet()
            .stream()
            .filter(entry -> entry.getKey()
                .startsWith(JOIN_KEY_PREFIX))
            .map(Map.Entry::getValue)
            .map(JoinCondition.class::cast)
            .map(joinCondition -> getOrCreateBatchLoader(environment, requestContext).load(joinCondition.getKey()))
            .collect(Collectors.toList());

        if (completableFutures.size() > 0) {
          if (completableFutures.size() == 1) {
            return completableFutures.get(0);
          }

          throw illegalStateException("Batching failed: found multiple join conditions!");
        }
      }

      var result = backendLoader.loadMany(collectionRequest, requestContext)
          .map(row -> row);

      if (isSubscription) {
        return result;
      }

      return result.collectList()
          .toFuture();
    }

    var objectRequest = requestFactory.createObjectRequest(executionStepInfo, environment.getSelectionSet());

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
    var executionStepInfo = requestFactory.getExecutionStepInfo(environment);

    var collectionRequest = requestFactory.createCollectionRequest(executionStepInfo, environment.getSelectionSet());

    MappedBatchLoader<Map<String, Object>, List<Map<String, Object>>> batchLoader = keys -> {
      var collectionBatchRequest = CollectionBatchRequest.builder()
          .collectionRequest(collectionRequest)
          .joinCriteria(JoinCriteria.builder()
              .keys(keys)
              .build())
          .build();

      return backendLoader.batchLoadMany(collectionBatchRequest, requestContext)
          .flatMap(group -> group.collectList()
              .map(rows -> Tuples.of(manipulateKey(group.key()), rows)))
          .collectMap(Tuple2::getT1, Tuple2::getT2)
          .toFuture();
    };

    return DataLoaderFactory.newMappedDataLoader(batchLoader);
  }

  private Map<String, Object> manipulateKey(Map<String, Object> key) {
    // TODO: Tijdelijke workaround: Equals check faalt omdat numerieke bij de eerste query als Long word
    // opgehaald en in tweede query als Int
    return key.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
          var v = entry.getValue();
          if (v instanceof Integer) {
            return ((Integer) v).longValue();
          }
          return v;
        }));
  }
}
