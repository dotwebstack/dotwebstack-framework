package org.dotwebstack.framework.core.backend;

import static org.dataloader.DataLoaderFactory.newMappedDataLoader;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isListType;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isSubscription;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.dataloader.DataLoader;
import org.dataloader.MappedBatchLoader;
import org.dotwebstack.framework.core.backend.validator.GraphQlValidator;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.RequestContext;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

class BackendDataFetcher implements DataFetcher<Object> {

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
    graphQlValidators.forEach(validator -> validator.validate(environment));

    Map<String, Object> source = environment.getSource();

    var executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo((environment));

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

      var joinKey = JOIN_KEY_PREFIX.concat(fieldName);
      if (source != null && source.containsKey(joinKey)) {
        var joinCondition = (JoinCondition) source.get(joinKey);

        return getOrCreateBatchLoader(environment, () -> createManyBatchLoader(environment, requestContext))
            .load(joinCondition.getKey());
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

    var keyField = Optional.of(requestContext)
        .map(RequestContext::getObjectField)
        .map(ObjectField::getKeyField)
        .orElse(null);

    if (source != null && source.containsKey(keyField)) {
      var key = getNestedMap(source, keyField);
      return getOrCreateBatchLoader(environment, () -> createSingleBatchLoader(environment, requestContext)).load(key);
    } else {
      return backendLoader.loadSingle(objectRequest, requestContext)
          .toFuture();
    }
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
      DataFetchingEnvironment environment, RequestContext requestContext) {
    var executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo(environment);

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
              .map(rows -> Tuples.of(group.key(), rows)))
          .collectMap(Tuple2::getT1, Tuple2::getT2)
          .toFuture();
    };

    return newMappedDataLoader(batchLoader);
  }

  private DataLoader<Map<String, Object>, Map<String, Object>> createSingleBatchLoader(
      DataFetchingEnvironment environment, RequestContext requestContext) {
    var executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo(environment);

    var objectRequest = requestFactory.createObjectRequest(executionStepInfo, environment.getSelectionSet());

    MappedBatchLoader<Map<String, Object>, Map<String, Object>> batchLoader = keys -> {

      var batchRequest = BatchRequest.builder()
          .objectRequest(objectRequest)
          .keys(keys)
          .build();

      return backendLoader.batchLoadSingle(batchRequest, requestContext)
          .collectMap(Tuple2::getT1, Tuple2::getT2)
          .toFuture();
    };

    return newMappedDataLoader(batchLoader);
  }
}
