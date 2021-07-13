package org.dotwebstack.framework.core.datafetchers;

import static graphql.language.OperationDefinition.Operation.SUBSCRIPTION;
import static java.util.Optional.ofNullable;
import static org.dataloader.DataLoader.newMappedDataLoader;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.internalServerErrorException;

import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.paging.PagingDataFetcherContext;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.RequestFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Conditional(GraphQlNativeEnabled.class)
@Component
@Slf4j
public final class GenericDataFetcher implements DataFetcher<Object> {

  public static final Map<String, Object> NULL_MAP = Map.of("id", "null");

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final Collection<BackendDataLoader> backendDataLoaders;

  private final RequestFactory requestFactory;

  public GenericDataFetcher(DotWebStackConfiguration dotWebStackConfiguration,
      Collection<BackendDataLoader> backendDataLoaders, RequestFactory requestFactory) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.backendDataLoaders = backendDataLoaders;
    this.requestFactory = requestFactory;
  }

  public Object get(DataFetchingEnvironment environment) {
    try {
      return doGet(environment);
    } catch (Exception e) {
      throw internalServerErrorException(e);
    }
  }

  private Object doGet(DataFetchingEnvironment environment) {
    TypeConfiguration<?> typeConfiguration = getTypeConfiguration(environment.getFieldType()).orElseThrow();

    Optional<LocalDataFetcherContext> localContext = getLocalContext(environment);
    Optional<Map<String, Object>> source = getSource(environment);
    var executionStepInfo = getExecutionStepInfo(environment);

    if (source.isPresent() && localContext.isPresent()) {
      return doNestedGet(environment, localContext.get(), source.get(), executionStepInfo);
    }

    var backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    var loadEnvironment = createLoadEnvironment(environment);

    var keyCondition = typeConfiguration.getKeyCondition(environment);

    // R: loadSingle (cardinality is one-to-one or many-to-one)
    // R2: Is key passed als field argument? (TBD: only supported for query field? source always null?)
    // => get key from field argument
    if (!loadEnvironment.isSubscription()
        && !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))) {

      if (backendDataLoader.useRequestApproach()) {
        var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

        return mapLoadSingle(typeConfiguration, backendDataLoader.loadSingleRequest(objectRequest));
      } else {
        return mapLoadSingle(typeConfiguration, backendDataLoader.loadSingle(keyCondition, loadEnvironment));
      }
    }

    Flux<DataFetcherResult<Object>> result;

    if (backendDataLoader.useRequestApproach()) {
      var collectionRequest = requestFactory.createCollectionRequest(typeConfiguration, environment);

      result = mapLoadMany(typeConfiguration, backendDataLoader.loadManyRequest(collectionRequest));
    } else {
      result = mapLoadMany(typeConfiguration, backendDataLoader.loadMany(keyCondition, loadEnvironment));
    }

    if (loadEnvironment.isSubscription()) {
      return result;
    }

    return result.collectList()
        .toFuture();
  }

  private Optional<LocalDataFetcherContext> getLocalContext(DataFetchingEnvironment environment) {
    if (environment.getLocalContext() instanceof PagingDataFetcherContext) {
      return Optional.ofNullable(((PagingDataFetcherContext) environment.getLocalContext()).getParentLocalContext());
    }
    return Optional.ofNullable(environment.getLocalContext());
  }

  private Optional<Map<String, Object>> getSource(DataFetchingEnvironment environment) {
    if (environment.getLocalContext() instanceof PagingDataFetcherContext) {
      return Optional.ofNullable(((PagingDataFetcherContext) environment.getLocalContext()).getParentSource());
    }
    return Optional.ofNullable(environment.getSource());
  }

  private ExecutionStepInfo getExecutionStepInfo(DataFetchingEnvironment environment) {
    if (environment.getLocalContext() instanceof PagingDataFetcherContext) {
      return environment.getExecutionStepInfo()
          .getParent();
    }
    return environment.getExecutionStepInfo();
  }

  private Object doNestedGet(DataFetchingEnvironment environment, LocalDataFetcherContext context,
      Map<String, Object> source, ExecutionStepInfo executionStepInfo) {
    TypeConfiguration<?> typeConfiguration = getTypeConfiguration(environment.getFieldType()).orElseThrow();

    String fieldName = executionStepInfo.getFieldDefinition()
        .getName();

    // Check if data is already present (eager-loaded)
    if (source.containsKey(fieldName)) {
      return createDataFetcherResult(typeConfiguration, source.get(fieldName));
    }

    // Create separate dataloader for every unique path, since evert path can have different arguments
    // or selection
    var dataLoaderKey = String.join("/", executionStepInfo.getPath()
        .getKeysOnly());

    // Retrieve dataloader instance for key, or create new instance when it does not exist yet
    DataLoader<Object, List<DataFetcherResult<Map<String, Object>>>> dataLoader = environment.getDataLoaderRegistry()
        .computeIfAbsent(dataLoaderKey, key -> this.createDataLoader(environment, typeConfiguration));

    var keyCondition = context.getKeyCondition(fieldName, typeConfiguration, source);

    return dataLoader.load(keyCondition);
  }

  private DataFetcherResult<Object> createDataFetcherResult(TypeConfiguration<?> typeConfiguration, Object data) {
    return DataFetcherResult.newResult()
        .data(data)
        .localContext(LocalDataFetcherContext.builder()
            .keyConditionFn(typeConfiguration::getKeyCondition)
            .build())
        .build();
  }

  private Optional<TypeConfiguration<?>> getTypeConfiguration(GraphQLOutputType outputType) {
    var nullableType = GraphQLTypeUtil.unwrapNonNull(outputType);
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(nullableType);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw new IllegalArgumentException("Output is not an object type.");
    }

    return ofNullable(dotWebStackConfiguration.getObjectTypes()
        .get(rawType.getName()));
  }

  private DataLoader<KeyCondition, ?> createDataLoader(DataFetchingEnvironment environment,
      TypeConfiguration<?> typeConfiguration) {
    var unwrappedType = environment.getExecutionStepInfo()
        .getUnwrappedNonNullType();

    var backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    var loadEnvironment = createLoadEnvironment(environment);

    if (GraphQLTypeUtil.isList(unwrappedType)) {
      if (backendDataLoader.useRequestApproach()) {
        var collectionRequest = requestFactory.createCollectionRequest(typeConfiguration, environment);

        return newMappedDataLoader(keys -> mapLoadBatchLoadMany(typeConfiguration,
            backendDataLoader.batchLoadManyRequest(keys, collectionRequest)).toFuture());
      } else {
        return newMappedDataLoader(
            keys -> mapLoadBatchLoadMany(typeConfiguration, backendDataLoader.batchLoadMany(keys, loadEnvironment))
                .toFuture());
      }
    }

    if (backendDataLoader.useRequestApproach()) {
      var objectRequest = requestFactory.createObjectRequest(typeConfiguration, environment);

      return newMappedDataLoader(
          keys -> mapLoadBatchLoadSingle(typeConfiguration, backendDataLoader.batchLoadSingleRequest(objectRequest)));
    } else {
      return newMappedDataLoader(
          keys -> mapLoadBatchLoadSingle(typeConfiguration, backendDataLoader.batchLoadSingle(keys, loadEnvironment)));
    }
  }

  private Flux<DataFetcherResult<Object>> mapLoadMany(TypeConfiguration<?> typeConfiguration,
      Flux<Map<String, Object>> flux) {
    return flux.map(data -> createDataFetcherResult(typeConfiguration, data))
        .onErrorMap(ExceptionHelper::internalServerErrorException);
  }

  private CompletableFuture<DataFetcherResult<Object>> mapLoadSingle(TypeConfiguration<?> typeConfiguration,
      Mono<Map<String, Object>> mono) {
    return mono.map(data -> createDataFetcherResult(typeConfiguration, data))
        .onErrorMap(ExceptionHelper::internalServerErrorException)
        .toFuture();
  }

  private Mono<Map<KeyCondition, List<?>>> mapLoadBatchLoadMany(TypeConfiguration<?> typeConfiguration,
      Flux<GroupedFlux<KeyCondition, Map<String, Object>>> flux) {
    return flux.flatMap(group -> group.map(data -> createDataFetcherResult(typeConfiguration, data))
        .collectList()
        .map(list -> Map.entry(group.key(), list.stream()
            .noneMatch(dataFetcherResult -> dataFetcherResult.getData() == NULL_MAP) ? list : List.of())))
        .onErrorMap(ExceptionHelper::internalServerErrorException)
        .collectMap(Map.Entry::getKey, Map.Entry::getValue);
  }

  private CompletableFuture<Map<KeyCondition, DataFetcherResult<Object>>> mapLoadBatchLoadSingle(
      TypeConfiguration<?> typeConfiguration, Flux<Tuple2<KeyCondition, Map<String, Object>>> flux) {
    return flux.collectMap(Tuple2::getT1, tuple -> createDataFetcherResult(typeConfiguration, tuple.getT2()))
        .onErrorMap(ExceptionHelper::internalServerErrorException)
        .toFuture();
  }

  private LoadEnvironment createLoadEnvironment(DataFetchingEnvironment environment) {
    return LoadEnvironment.builder()
        .queryName(environment.getFieldDefinition()
            .getName())
        .executionStepInfo(environment.getExecutionStepInfo())
        .selectionSet(environment.getSelectionSet())
        .subscription(SUBSCRIPTION.equals(environment.getOperationDefinition()
            .getOperation()))
        .build();
  }

  private Optional<BackendDataLoader> getBackendDataLoader(TypeConfiguration<?> typeConfiguration) {
    return backendDataLoaders.stream()
        .filter(loader -> loader.supports(typeConfiguration))
        .findFirst();
  }
}
