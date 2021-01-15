package org.dotwebstack.framework.core.datafetchers;

import static java.util.Optional.ofNullable;

import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.keys.FieldKey;
import org.dotwebstack.framework.core.datafetchers.keys.Key;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@Component
@Slf4j
public final class GenericDataFetcher implements DataFetcher<Object> {

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
      String resultKey = executionStepInfo.getResultKey();

      // Check if data is already present (eager-loaded)
      if (source.containsKey(resultKey)) {
        return source.get(resultKey);
      }

      // Create separate dataloader for every unique path, since evert path can have different arguments
      // or selection
      String dataLoaderKey = String.join("/", executionStepInfo.getPath()
          .getKeysOnly());

      // Retrieve dataloader instance for key, or create new instance when it does not exist yet
      DataLoader<Object, List<Map<String, Object>>> dataLoader = environment.getDataLoaderRegistry()
          .computeIfAbsent(dataLoaderKey, key -> this.createDataLoader(environment, typeConfiguration));

      String fieldName = environment.getFieldDefinition()
          .getName();

      if (source.containsKey(fieldName)) {
        return dataLoader.load(source.get(fieldName));
      }
    }

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getFieldType());
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .selectedFields(environment.getSelectionSet()
            .getImmediateFields())
        .build();

    Optional<Key> key = getKey(environment);

    // R: loadSingle (cardinality is one-to-one or many-to-one)
    // R2: Is key passed als field argument? (TBD: only supported for query field? source always null?)
    // => get key from field argument
    if (key.isPresent() || !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))) {
      return backendDataLoader.loadSingle(key.orElse(null), loadEnvironment)
          .map(objectResult -> DataFetcherResult.newResult()
              .data(objectResult)
              .build())
          .toFuture();
    }

    return backendDataLoader.loadMany(null, loadEnvironment)
        .collectList()
        .map(objectResult -> DataFetcherResult.newResult()
            .data(objectResult)
            .build())
        .toFuture();
  }

  private Optional<TypeConfiguration<?>> getTypeConfiguration(GraphQLOutputType outputType) {
    GraphQLType nullableType = GraphQLTypeUtil.unwrapNonNull(outputType);
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(nullableType);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw new IllegalArgumentException("Output is not an object type.");
    }

    return ofNullable(dotWebStackConfiguration.getTypeMapping()
        .get(rawType.getName()));
  }

  private Optional<Key> getKey(DataFetchingEnvironment environment) {
    List<FieldKey> fieldKeys = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> argument.getDirectives("key") != null)
        .map(argument -> getFieldKey(environment, argument))
        .collect(Collectors.toList());

    if (fieldKeys.size() == 1) {
      return Optional.of(fieldKeys.get(0));
    }

    return Optional.empty();
  }

  private FieldKey getFieldKey(DataFetchingEnvironment environment, GraphQLArgument argument) {
    // FIXME: magic word
    GraphQLDirective directive = argument.getDirective("key");

    String keyName = argument.getName();
    if (directive.getArgument("field") != null) {
      keyName = directive.getArgument("field")
          .getValue()
          .toString();
    }

    Object keyValue = environment.getArguments()
        .get(keyName);

    return FieldKey.builder()
        .name(keyName)
        .value(keyValue)
        .build();
  }

  private DataLoader<Object, ?> createDataLoader(DataFetchingEnvironment environment,
      TypeConfiguration<?> typeConfiguration) {
    GraphQLOutputType unwrappedType = environment.getExecutionStepInfo()
        .getUnwrappedNonNullType();
    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(unwrappedType);
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .selectedFields(environment.getSelectionSet()
            .getImmediateFields())
        .build();

    if (GraphQLTypeUtil.isList(unwrappedType)) {
      return DataLoader.newDataLoader(requests -> backendDataLoader.batchLoadMany(requests, loadEnvironment)
          .flatMapSequential(Flux::collectList)
          .collectList()
          .toFuture());
    }

    return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadSingle(keys, loadEnvironment)
        .collectMap(Tuple2::getT1, Tuple2::getT2)
        .toFuture());
  }

  private Optional<BackendDataLoader> getBackendDataLoader(TypeConfiguration<?> typeConfiguration) {
    return backendDataLoaders.stream()
        .filter(loader -> loader.supports(typeConfiguration))
        .findFirst();
  }
}
