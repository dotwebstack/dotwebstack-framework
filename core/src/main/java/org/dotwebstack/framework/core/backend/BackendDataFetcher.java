package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static org.dataloader.DataLoaderFactory.newMappedDataLoader;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_BATCH_KEY_QUERY;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getKeyArguments;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isListType;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isSubscription;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLTypeUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.MappedBatchLoader;
import org.dotwebstack.framework.core.backend.validator.GraphQlValidator;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.model.Settings;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

class BackendDataFetcher implements DataFetcher<Object> {

  private final BackendLoader backendLoader;

  private final BackendRequestFactory requestFactory;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

  private final List<GraphQlValidator> graphQlValidators;

  private final Settings settings;

  public BackendDataFetcher(BackendLoader backendLoader, BackendRequestFactory requestFactory,
      BackendExecutionStepInfo backendExecutionStepInfo, List<GraphQlValidator> graphQlValidators, Settings settings) {
    this.backendLoader = backendLoader;
    this.requestFactory = requestFactory;
    this.backendExecutionStepInfo = backendExecutionStepInfo;
    this.graphQlValidators = graphQlValidators;
    this.settings = settings;
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
    var additionalData = environment.getFieldDefinition()
        .getDefinition()
        .getAdditionalData();

    if (isSubscription || isListType(environment.getFieldType())) {
      if (additionalData.containsKey(IS_BATCH_KEY_QUERY)) {
        return executeBatchQueryWithKeys(environment, requestContext);
      }

      var collectionRequest = requestFactory.createCollectionRequest(executionStepInfo, environment.getSelectionSet());

      var joinKey = JOIN_KEY_PREFIX.concat(fieldName);
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

  private List<?> executeBatchQueryWithKeys(DataFetchingEnvironment environment, RequestContext requestContext) {
    DataLoader<Map<String, Object>, ?> batchLoader;

    batchLoader = getDataLoaderForBatchKeyQuery(environment, requestContext);

    getKeyArguments(environment.getFieldDefinition()).forEach(argument -> {

      var argumentValue = environment.getArguments()
          .get(argument.getName());

      var keys = argumentValue == null ? List.of() : castToList(argumentValue);

      validateBatchKeys(keys);

      keys.forEach(keyValue -> batchLoader.load(Map.of(argument.getName(), keyValue)));
    });

    return batchLoader.dispatchAndJoin();
  }

  private void validateBatchKeys(List<Object> keys) {
    if (keys.isEmpty()) {
      throw requestValidationException("At least one key must be provided");
    }

    if (keys.size() > settings.getMaxBatchKeySize()) {
      throw requestValidationException("Got {} keys but a maximum of {} keys is allowed!", keys.size(),
          settings.getMaxBatchKeySize());
    }

    var duplicateKeys = keys.stream()
        .filter(key -> Collections.frequency(keys, key) > 1)
        .distinct()
        .collect(Collectors.toList());
    if (!duplicateKeys.isEmpty()) {
      throw requestValidationException("The following keys are duplicate: {}", duplicateKeys);
    }
  }

  private DataLoader<Map<String, Object>, ?> getDataLoaderForBatchKeyQuery(DataFetchingEnvironment environment,
      RequestContext requestContext) {
    DataLoader<Map<String, Object>, ?> batchLoader;
    var outputType = Optional.of(environment.getFieldDefinition()
        .getType())
        .filter(TypeHelper::isListType)
        .map(GraphQLTypeUtil::unwrapNonNull)
        .map(GraphQLList.class::cast)
        .orElseThrow(() -> illegalStateException("Batch output type needs to be a list!"));

    if (isListType(unwrapNonNull(outputType.getWrappedType()))) {
      batchLoader = createManyBatchLoader(environment, requestContext, null);
    } else {
      batchLoader = createSingleBatchLoader(environment, requestContext);
    }
    return batchLoader;
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
        .setMaxBatchSize(settings.getMaxBatchSize()));
  }

  private DataLoader<Map<String, Object>, Map<String, Object>> createSingleBatchLoader(
      DataFetchingEnvironment environment, RequestContext requestContext) {
    var executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo(environment);

    var objectRequest =
        (SingleObjectRequest) requestFactory.createObjectRequest(executionStepInfo, environment.getSelectionSet());

    MappedBatchLoader<Map<String, Object>, Map<String, Object>> batchLoader = keys -> {

      var batchRequest = BatchRequest.builder()
          .objectRequest(objectRequest)
          .keys(keys)
          .build();

      return backendLoader.batchLoadSingle(batchRequest, requestContext)
          .collectMap(Tuple2::getT1, objects -> objects.getT2() != BackendLoader.NILL_MAP ? objects.getT2() : null,
              HashMap::new)
          .toFuture();
    };

    return newMappedDataLoader(batchLoader);
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
