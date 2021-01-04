package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dataloader.DataLoader;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@Component
public final class GenericDataFetcher implements DataFetcher<Object> {

  private final Collection<BackendDataLoader> backendDataLoaders;

  public GenericDataFetcher(Collection<BackendDataLoader> backendDataLoaders) {
    this.backendDataLoaders = backendDataLoaders;
  }

  public Object get(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo = environment.getExecutionStepInfo();
    Map<String, Object> source = environment.getSource();

    // ID argument (alleen op root level?)
    // Source object met FK (owning side)
    // Source object met PK (non-owning side)
    // Geen van allen

    if (source != null) {
      String dataLoaderKey = String.join("/", executionStepInfo.getPath().getKeysOnly());

      DataLoader<Object, List<Map<String, Object>>> dataLoader = environment.getDataLoaderRegistry()
          .computeIfAbsent(dataLoaderKey, key -> this.createDataLoader(environment));

      String fieldName = environment.getFieldDefinition().getName();

      return dataLoader.load(source.get(fieldName));
    }

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getFieldType());
    BackendDataLoader backendDataLoader = getBackendDataLoader(objectType).orElseThrow();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .objectType(objectType)
        .build();

    return backendDataLoader.loadMany(null, loadEnvironment)
        .collectList()
        .toFuture();
  }

  private DataLoader<Object, ?> createDataLoader(DataFetchingEnvironment environment) {
    GraphQLOutputType unwrappedType = environment.getExecutionStepInfo().getUnwrappedNonNullType();
    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(unwrappedType);
    BackendDataLoader backendDataLoader = getBackendDataLoader(objectType).orElseThrow();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .objectType(objectType)
        .build();

//    if (GraphQLTypeUtil.isList(unwrappedType)) {
//      return DataLoader.newDataLoader(requests ->
//          backendDataLoader.batchLoadMany(requests, loadEnvironment)
//              .flatMapSequential(Flux::collectList)
//              .collectList()
//              .toFuture());
//    }

    return DataLoader.newMappedDataLoader(keys ->
        backendDataLoader.batchLoadSingle(Flux.fromIterable(keys), loadEnvironment)
            .collectMap(Tuple2::getT1, Tuple2::getT2)
            .toFuture());
  }

  private Optional<BackendDataLoader> getBackendDataLoader(GraphQLObjectType objectType) {
    return backendDataLoaders.stream()
        .filter(loader -> loader.supports(objectType))
        .findFirst();
  }
}
