package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dataloader.DataLoader;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@Component
public final class GenericDataFetcher implements DataFetcher<Object> {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final Collection<BackendDataLoader> backendDataLoaders;

  public GenericDataFetcher(DotWebStackConfiguration dotWebStackConfiguration, Collection<BackendDataLoader> backendDataLoaders) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.backendDataLoaders = backendDataLoaders;
  }

  public Object get(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo = environment.getExecutionStepInfo();
    Map<String, Object> source = environment.getSource();

    // TODO: improve type safety & error handling
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    GraphQLObjectType rawType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(outputType);
    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeMapping()
        .get(rawType.getName());

    System.out.println(typeConfiguration);

    // Loop through keys (max 1 currently)
    // Check presence of argument with corresponding field name
    // If present, apply key condition
    // Validate field is non-list object field

    if (source != null) {
      String dataLoaderKey = String.join("/", executionStepInfo.getPath()
          .getKeysOnly());

      DataLoader<Object, List<Map<String, Object>>> dataLoader = environment.getDataLoaderRegistry()
          .computeIfAbsent(dataLoaderKey, key -> this.createDataLoader(environment, typeConfiguration));

      String fieldName = environment.getFieldDefinition()
          .getName();

      return dataLoader.load(source.get(fieldName));
    }

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getFieldType());
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment<?> loadEnvironment = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .build();

    return backendDataLoader.loadMany(null, loadEnvironment)
        .collectList()
        .toFuture();
  }

  private DataLoader<Object, ?> createDataLoader(DataFetchingEnvironment environment, TypeConfiguration<?> typeConfiguration) {
    GraphQLOutputType unwrappedType = environment.getExecutionStepInfo()
        .getUnwrappedNonNullType();
    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(unwrappedType);
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment<?> loadEnvironment = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .build();

    // if (GraphQLTypeUtil.isList(unwrappedType)) {
    // return DataLoader.newDataLoader(requests ->
    // backendDataLoader.batchLoadMany(requests, loadEnvironment)
    // .flatMapSequential(Flux::collectList)
    // .collectList()
    // .toFuture());
    // }

    return DataLoader
        .newMappedDataLoader(keys -> backendDataLoader.batchLoadSingle(Flux.fromIterable(keys), loadEnvironment)
            .collectMap(Tuple2::getT1, Tuple2::getT2)
            .toFuture());
  }

  private Optional<BackendDataLoader> getBackendDataLoader(TypeConfiguration<? extends FieldConfiguration> typeConfiguration) {
    return backendDataLoaders.stream()
        .filter(loader -> loader.supports(typeConfiguration))
        .findFirst();
  }
}
