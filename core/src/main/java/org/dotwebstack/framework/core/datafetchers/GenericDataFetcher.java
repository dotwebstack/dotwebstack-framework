package org.dotwebstack.framework.core.datafetchers;

import static graphql.language.OperationDefinition.Operation.SUBSCRIPTION;
import static java.util.Optional.ofNullable;

import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@Component
@Slf4j
public final class GenericDataFetcher implements DataFetcher<Object> {

  public static final Map<String, Object> NULL_MAP = Map.of("id", "null");

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final Collection<BackendDataLoader> backendDataLoaders;

  public GenericDataFetcher(DotWebStackConfiguration dotWebStackConfiguration,
      Collection<BackendDataLoader> backendDataLoaders) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.backendDataLoaders = backendDataLoaders;
  }

  public Object get(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo = environment.getExecutionStepInfo();
    Map<String, Object> source = environment.getSource();
    TypeConfiguration<?> typeConfiguration = getTypeConfiguration(environment.getFieldType()).orElseThrow();

    if (source != null) {
      String fieldName = executionStepInfo.getFieldDefinition()
          .getName();

      // Check if data is already present (eager-loaded)
      if (source.containsKey(fieldName)) {
        return createDataFetcherResult(typeConfiguration, source.get(fieldName));
      }

      // Create separate dataloader for every unique path, since evert path can have different arguments
      // or selection
      String dataLoaderKey = String.join("/", executionStepInfo.getPath()
          .getKeysOnly());

      // Retrieve dataloader instance for key, or create new instance when it does not exist yet
      DataLoader<Object, List<DataFetcherResult<Map<String, Object>>>> dataLoader = environment.getDataLoaderRegistry()
          .computeIfAbsent(dataLoaderKey, key -> this.createDataLoader(environment, typeConfiguration));

      LocalDataFetcherContext context = environment.getLocalContext();
      KeyCondition keyCondition = context.getKeyCondition(fieldName, typeConfiguration, source);

      return dataLoader.load(keyCondition);
    }

    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = createLoadEnvironment(environment);

    KeyCondition keyCondition = typeConfiguration.getKeyCondition(environment);

    // R: loadSingle (cardinality is one-to-one or many-to-one)
    // R2: Is key passed als field argument? (TBD: only supported for query field? source always null?)
    // => get key from field argument
    if (!loadEnvironment.isSubscription()
        && !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))) {
      return backendDataLoader.loadSingle(keyCondition, loadEnvironment)
          .map(data -> createDataFetcherResult(typeConfiguration, data))
          .toFuture();
    }

    Flux<DataFetcherResult<Object>> result = backendDataLoader.loadMany(keyCondition, loadEnvironment)
        .map(data -> createDataFetcherResult(typeConfiguration, data));

    if (loadEnvironment.isSubscription()) {
      return result;
    }

    return result.collectList()
        .toFuture();
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
    GraphQLType nullableType = GraphQLTypeUtil.unwrapNonNull(outputType);
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(nullableType);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw new IllegalArgumentException("Output is not an object type.");
    }

    return ofNullable(dotWebStackConfiguration.getObjectTypes()
        .get(rawType.getName()));
  }

  private DataLoader<KeyCondition, ?> createDataLoader(DataFetchingEnvironment environment,
      TypeConfiguration<?> typeConfiguration) {
    GraphQLOutputType unwrappedType = environment.getExecutionStepInfo()
        .getUnwrappedNonNullType();

    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = createLoadEnvironment(environment);

    if (GraphQLTypeUtil.isList(unwrappedType)) {
      return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadMany(keys, loadEnvironment)
          .flatMap(group -> group.map(data -> createDataFetcherResult(typeConfiguration, data))
              .collectList()
              .map(list -> Map.entry(group.key(), list.stream()
                  .noneMatch(dataFetcherResult -> dataFetcherResult.getData() == NULL_MAP) ? list : List.of())))
          .collectMap(Map.Entry::getKey, Map.Entry::getValue)
          .toFuture());
    }

    return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadSingle(keys, loadEnvironment)
        .collectMap(Tuple2::getT1, tuple -> createDataFetcherResult(typeConfiguration, tuple.getT2()))
        .toFuture());
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
