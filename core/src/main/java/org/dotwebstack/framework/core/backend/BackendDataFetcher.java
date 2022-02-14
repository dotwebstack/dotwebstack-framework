package org.dotwebstack.framework.core.backend;

import static org.dataloader.DataLoaderFactory.newMappedDataLoader;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isListType;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isSubscription;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.MappedBatchLoader;
import org.dotwebstack.framework.core.backend.validator.GraphQlValidator;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.RequestContext;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

class BackendDataFetcher implements DataFetcher<Object> {

  private static final int MAX_BATCH_SIZE = 250;

  private final BackendLoader backendLoader;

  private final BackendRequestFactory requestFactory;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

  private final List<GraphQlValidator> graphQlValidators;

  public BackendDataFetcher(BackendLoader backendLoader, BackendRequestFactory requestFactory,
      BackendExecutionStepInfo backendExecutionStepInfo, List<GraphQlValidator> graphQlValidators) {
    this.backendLoader = backendLoader;
    this.requestFactory = requestFactory;
    this.backendExecutionStepInfo = backendExecutionStepInfo;
    this.graphQlValidators = graphQlValidators;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    Map<String, Object> source = environment.getSource();

    var executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo((environment));

    var fieldName = executionStepInfo.getField()
        .getName();

    String lookupName = getLookupName(executionStepInfo, fieldName);

    // Data was eager-loaded by parent
    if (source != null && source.containsKey(lookupName)) {
      return source.get(lookupName);
    }

    if (backendLoader == null) {
      throw illegalStateException("BackendLoader can't be null.");
    }

    graphQlValidators.forEach(validator -> validator.validate(environment));

    var isSubscription = isSubscription(environment.getOperationDefinition());
    var requestContext = requestFactory.createRequestContext(environment);

    if (isSubscription || isListType(environment.getFieldType())) {
      var collectionRequest = requestFactory.createCollectionRequest(executionStepInfo, environment.getSelectionSet());

      var joinKey = JOIN_KEY_PREFIX.concat(lookupName);
      if (source != null && source.containsKey(joinKey)) {
        var joinCondition = (JoinCondition) source.get(joinKey);

        if (joinCondition.getKey()
            .isEmpty()) {
          return List.of();
        }

        return getOrCreateBatchLoader(environment,
            () -> createManyBatchLoader(environment, requestContext, joinCondition)).load(joinCondition.getKey());
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

  private <K, V> DataLoader<K, V> getOrCreateBatchLoader(DataFetchingEnvironment environment,
      Supplier<DataLoader<K, V>> supplier) {
    // Create separate data loader for every unique path, since every path can have different arguments
    // or selection
    var dataLoaderKey = String.join("/", environment.getExecutionStepInfo()
        .getPath()
        .getKeysOnly());

    return environment.getDataLoaderRegistry()
        .computeIfAbsent(dataLoaderKey, key -> supplier.get());
  }

  private DataLoader<Map<String, Object>, List<Map<String, Object>>> createManyBatchLoader(
      DataFetchingEnvironment environment, RequestContext requestContext, JoinCondition joinCondition) {
    var executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo(environment);

    var collectionRequest = requestFactory.createCollectionRequest(executionStepInfo, environment.getSelectionSet());

    MappedBatchLoader<Map<String, Object>, List<Map<String, Object>>> batchLoader = keys -> {
      var collectionBatchRequest = CollectionBatchRequest.builder()
          .collectionRequest(collectionRequest)
          .joinCriteria(JoinCriteria.builder()
              .keys(keys)
              .joinCondition(joinCondition)
              .build())
          .build();

      return backendLoader.batchLoadMany(collectionBatchRequest, requestContext)
          .flatMap(group -> group.collectList()
              .map(rows -> Tuples.of(group.key(), rows)))
          .collectMap(Tuple2::getT1, Tuple2::getT2)
          .toFuture();
    };

    return newMappedDataLoader(batchLoader, DataLoaderOptions.newOptions()
        .setMaxBatchSize(MAX_BATCH_SIZE));
  }

  private String getLookupName(ExecutionStepInfo executionStepInfo, String fieldName) {
    return !executionStepInfo.getField()
        .getResultKey()
        .equals(fieldName) ? String.format("%s.%s", fieldName,
            executionStepInfo.getField()
                .getResultKey())
            : fieldName;
  }
}
