package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.dataloader.DataLoader;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static graphql.language.OperationDefinition.Operation.SUBSCRIPTION;
import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.datafetchers.FieldConstants.RDF_URI_FIELD;

@Component @Slf4j public final class GenericDataFetcher implements DataFetcher<Object> {

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
      String fieldName = executionStepInfo.getFieldDefinition().getName();

      // Check if data is already present (eager-loaded)
      if (source.containsKey(fieldName)) {
        return createDataFetcherResult(typeConfiguration, source.get(fieldName));
      }

      // Create separate dataloader for every unique path, since evert path can have different arguments
      // or selection
      String dataLoaderKey = String.join("/", executionStepInfo.getPath().getKeysOnly());

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
    if (!loadEnvironment.isSubscription() && !GraphQLTypeUtil.isList(
        GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))) {
      return backendDataLoader.loadSingle(keyCondition, loadEnvironment)
          .map(data -> addRdfUriForTypes(data, typeConfiguration, environment.getSelectionSet()))
          .map(data -> createDataFetcherResult(typeConfiguration, data))
          .toFuture();
    }

    Flux<DataFetcherResult<Object>> result = backendDataLoader.loadMany(keyCondition, loadEnvironment)
        .map(data -> createDataFetcherResult(typeConfiguration, data));

    if (loadEnvironment.isSubscription()) {
      return result;
    }

    return result.collectList().toFuture();
  }

  private DataFetcherResult<Object> createDataFetcherResult(TypeConfiguration<?> typeConfiguration,
      Object data) {
    return DataFetcherResult.newResult()
        .data(data)
        .localContext(LocalDataFetcherContext.builder().keyConditionFn(typeConfiguration::getKeyCondition).build())
        .build();
  }

  private Map<String, Object> addRdfUriForTypes(Map<String, Object> data,
      TypeConfiguration<?> typeConfiguration, DataFetchingFieldSelectionSet selectionSet) {
    final Map<String, Object> result = new HashMap();

    // first add the rdf uri of the root entry _id
    addRdfUri(data, typeConfiguration);

    data.entrySet().stream().forEach(entry -> {
      Optional<TypeConfiguration<?>> optionalCurrentTypeConfiguration = getTypeConfiguration(entry.getKey(), selectionSet);
      if (optionalCurrentTypeConfiguration.isPresent() && entry.getValue() != null) {
        // TODO: howto fix the casting to a map
        result.put(entry.getKey(),
            addRdfUri((Map<String, Object>) entry.getValue(), optionalCurrentTypeConfiguration.get()));
      } else {
        result.put(entry.getKey(), entry.getValue());
      }
    });
    return result;
  }

  // TODO check DefaultSelectWrapperBuilder UnaryOperator (MultiSelectRowAssembler)
  private Map<String, Object> addRdfUri(Map<String, Object> data, TypeConfiguration<?> currentTypeConfiguration) {
    String uriTemplate = currentTypeConfiguration.getUriTemplate();
    if (!StringUtils.isBlank(uriTemplate)) {
      StringSubstitutor substitutor = new StringSubstitutor(data);
      String rdfUri = substitutor.replace(uriTemplate);
      // TODO: constante maken voor _id, maar waar past dat het beste
      data.put(RDF_URI_FIELD, rdfUri);
    }
    return data;
  }

  // TODO: check TypeCOnfiguration vs AbstractTypeConfiguration
  private Optional<TypeConfiguration<?>> getTypeConfiguration(String fieldName,
      DataFetchingFieldSelectionSet selectionSet) {
    if(selectionSet.contains(fieldName)) {
      GraphQLOutputType qlOutputType = selectionSet.getFields(fieldName).get(0).getFieldDefinition().getType();

      Optional<TypeConfiguration<?>> optionalTypeConfiguration = getTypeConfiguration(qlOutputType);

      return optionalTypeConfiguration;
    }
    return Optional.empty();
  }

  private Optional<TypeConfiguration<?>> getTypeConfiguration(GraphQLOutputType outputType) {
    GraphQLType nullableType = GraphQLTypeUtil.unwrapNonNull(outputType);
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(nullableType);

//    if (!(rawType instanceof GraphQLObjectType)) {
//      throw new IllegalArgumentException("Output is not an object type.");
//    }

    return ofNullable(dotWebStackConfiguration.getTypeMapping().get(rawType.getName()));
  }

  private DataLoader<KeyCondition, ?> createDataLoader(DataFetchingEnvironment environment,
      TypeConfiguration<?> typeConfiguration) {
    GraphQLOutputType unwrappedType = environment.getExecutionStepInfo().getUnwrappedNonNullType();

    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = createLoadEnvironment(environment);

    if (GraphQLTypeUtil.isList(unwrappedType)) {
      return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadMany(keys, loadEnvironment)
          .flatMap(group -> group.map(data -> createDataFetcherResult(typeConfiguration, data))
              .collectList()
              .map(list -> Map.entry(group.key(), list)))
          .collectMap(Map.Entry::getKey, Map.Entry::getValue)
          .toFuture());
    }

    return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadSingle(keys, loadEnvironment)
        .collectMap(Tuple2::getT1, tuple -> createDataFetcherResult(typeConfiguration, tuple.getT2()))
        .toFuture());
  }

  private LoadEnvironment createLoadEnvironment(DataFetchingEnvironment environment) {
    return LoadEnvironment.builder()
        .queryName(environment.getFieldDefinition().getName())
        .executionStepInfo(environment.getExecutionStepInfo())
        .selectionSet(new SelectionSetWrapper(environment.getSelectionSet()))
        .subscription(SUBSCRIPTION.equals(environment.getOperationDefinition().getOperation()))
        .build();
  }

  private Optional<BackendDataLoader> getBackendDataLoader(TypeConfiguration<?> typeConfiguration) {
    return backendDataLoaders.stream().filter(loader -> loader.supports(typeConfiguration)).findFirst();
  }
}
