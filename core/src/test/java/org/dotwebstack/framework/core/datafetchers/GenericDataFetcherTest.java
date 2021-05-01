package org.dotwebstack.framework.core.datafetchers;

import static graphql.language.OperationDefinition.Operation.QUERY;
import static graphql.language.OperationDefinition.Operation.SUBSCRIPTION;
import static graphql.language.OperationDefinition.newOperationDefinition;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


@ExtendWith(MockitoExtension.class)
class GenericDataFetcherTest {

  @Mock
  private GraphQLFieldDefinition graphQlFieldDefinitionMock;

  @Mock
  private AbstractTypeConfiguration<?> typeConfiguration;

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  private BackendDataLoader backendDataLoader;

  @Mock
  private ExecutionStepInfo executionStepInfo;

  private GenericDataFetcher genericDataFetcher;

  private DataLoaderRegistry dataLoaderRegistry;

  @BeforeEach
  void doBeforeEach() {
    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("Brewery", typeConfiguration));

    when(graphQlFieldDefinitionMock.getName()).thenReturn("brewery");

    dataLoaderRegistry = new DataLoaderRegistry();

    genericDataFetcher = new GenericDataFetcher(dotWebStackConfiguration, List.of(backendDataLoader));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsFuture_forLoadSingleQueryOperation() throws Exception {
    Map<String, Object> data = Map.of("identifier", "id-1");

    when(backendDataLoader.loadSingle(any(), any())).thenReturn(Mono.just(data));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);

    GraphQLOutputType outputType = createBreweryType();

    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    Object future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    DataFetcherResult<Map<String, Object>> result = (DataFetcherResult<Map<String, Object>>) ((Future<?>) future).get();

    assertThat(Optional.of(result)
        .map(DataFetcherResult::getData)
        .orElseThrow()
        .entrySet(), equalTo(data.entrySet()));

    verify(backendDataLoader).loadSingle(isNull(), any(LoadEnvironment.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsFuture_ForLoadManyQueryOperation() throws Exception {
    List<Map<String, Object>> data = List.of(Map.of("identifier", "id-1"), Map.of("identifier", "id-2"));
    GraphQLOutputType outputType = GraphQLList.list(createBreweryType());
    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadMany(any(), any())).thenReturn(Flux.fromIterable(data));

    Object future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    List<DataFetcherResult<Map<String, Object>>> result =
        (List<DataFetcherResult<Map<String, Object>>>) ((Future<?>) future).get();

    assertThat(result.stream()
        .map(DataFetcherResult::getData)
        .collect(Collectors.toList()), equalTo(data));

    verify(backendDataLoader).loadMany(isNull(), any(LoadEnvironment.class));
  }

  @Test
  void get_returnsFlux_forLoadManySubscriptionOperation() {
    GraphQLOutputType outputType = GraphQLList.list(createBreweryType());
    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, SUBSCRIPTION);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadMany(any(), any())).thenReturn(Flux.empty());

    Object result = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, instanceOf(Flux.class));

    verify(backendDataLoader).loadMany(isNull(), any(LoadEnvironment.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsDatafetcherResult_ForEagerLoadedNestedQueryOperation() {
    Map<String, Object> data = Map.of("identifier", "id-1");
    GraphQLOutputType outputType = GraphQLList.list(createBreweryType());
    Map<String, Object> source = Map.of("brewery", data);
    DataFetchingEnvironment dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, source);

    when(dataFetchingEnvironment.getExecutionStepInfo()
        .getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);

    DataFetcherResult<Map<String, Object>> result =
        (DataFetcherResult<Map<String, Object>>) genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, notNullValue());
    assertThat(result.getData()
        .entrySet(), equalTo(data.entrySet()));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsDatafetcherResult_ForBatchLoadManyQueryOperation() throws Exception {
    KeyCondition keyConditionWithBeer = TestKeyCondition.builder()
        .valueMap(Map.of("brewery", "id-brewery-1"))
        .build();
    KeyCondition keyConditionWithoutBeer = TestKeyCondition.builder()
        .valueMap(Map.of("brewery", "id-brewery-2"))
        .build();

    Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadManyResult = Flux.fromIterable(List.of(
        new KeyConditionGroupedFlux(keyConditionWithBeer, Flux.fromIterable(List.of(Map.of("id", "id-1")))),
        new KeyConditionGroupedFlux(keyConditionWithoutBeer, Flux.fromIterable(List.of(GenericDataFetcher.NULL_MAP)))));

    GraphQLOutputType outputType = GraphQLList.list(GraphQLObjectType.newObject()
        .name("Beers")
        .build());

    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("Beers", typeConfiguration));
    when(backendDataLoader.batchLoadMany(any(), any())).thenReturn(batchLoadManyResult);
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(executionStepInfo.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.parse("/my/beers"));
    when(executionStepInfo.getUnwrappedNonNullType()).thenReturn(outputType);

    DataFetchingEnvironment dataFetchingEnvironment =
        createDataFetchingEnvironment(outputType, QUERY, Map.of(), keyConditionWithBeer);
    final Future<?> futureWithBeer = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), keyConditionWithoutBeer);
    final Future<?> futureWithoutBeer = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    DataLoader<?, ?> dataLoader = dataFetchingEnvironment.getDataLoader("my/beers");

    dataLoader.dispatch();

    List<DataFetcherResult<Map<String, Object>>> futureWithBeerResult =
        (List<DataFetcherResult<Map<String, Object>>>) futureWithBeer.get();
    assertThat(futureWithBeerResult.size(), is(1));
    assertThat(futureWithBeerResult.get(0)
        .getData()
        .get("id"), is("id-1"));

    List<DataFetcherResult<Map<String, Object>>> futureWithoutBeerResult =
        (List<DataFetcherResult<Map<String, Object>>>) futureWithoutBeer.get();
    assertThat(futureWithoutBeerResult.size(), is(0));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsDatafetcherResult_ForBatchLoadSingleQueryOperation() throws Exception {
    GraphQLOutputType outputType = createBreweryType();

    KeyCondition breweryOneKey = TestKeyCondition.builder()
        .valueMap(Map.of("id", "id-1"))
        .build();
    Map<String, Object> breweryOneData = Map.of("id", "id-1");

    KeyCondition breweryTwoKey = TestKeyCondition.builder()
        .valueMap(Map.of("id", "id-2"))
        .build();
    Map<String, Object> breweryTwoData = Map.of("id", "id-2");

    List<Tuple2<KeyCondition, Map<String, Object>>> breweries =
        List.of(Tuples.of(breweryOneKey, breweryOneData), Tuples.of(breweryTwoKey, breweryTwoData));

    when(backendDataLoader.batchLoadSingle(any(), any())).thenReturn(Flux.fromIterable(breweries));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(executionStepInfo.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.parse("/my/brewery"));
    when(executionStepInfo.getUnwrappedNonNullType()).thenReturn(outputType);

    DataFetchingEnvironment dataFetchingEnvironment =
        createDataFetchingEnvironment(outputType, QUERY, Map.of(), breweryOneKey);
    final Future<?> breweryOneFuture = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), breweryTwoKey);
    final Future<?> breweryTwoFuture = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    DataLoader<?, ?> dataLoader = dataFetchingEnvironment.getDataLoader("my/brewery");

    dataLoader.dispatch();

    DataFetcherResult<Map<String, Object>> futureResult =
        (DataFetcherResult<Map<String, Object>>) breweryOneFuture.get();
    assertThat(futureResult.getData()
        .get("id"), is("id-1"));

    futureResult = (DataFetcherResult<Map<String, Object>>) breweryTwoFuture.get();
    assertThat(futureResult.getData()
        .get("id"), is("id-2"));
  }

  private DataFetchingEnvironment createDataFetchingEnvironment(GraphQLOutputType outputType,
      OperationDefinition.Operation operation) {
    return createDataFetchingEnvironment(outputType, operation, null);
  }

  private DataFetchingEnvironment createDataFetchingEnvironment(GraphQLOutputType outputType,
      OperationDefinition.Operation operation, Object source) {
    return createDataFetchingEnvironment(outputType, operation, source, mock(KeyCondition.class));
  }

  private DataFetchingEnvironment createDataFetchingEnvironment(GraphQLOutputType outputType,
      OperationDefinition.Operation operation, Object source, KeyCondition keyCondition) {
    return newDataFetchingEnvironment().executionStepInfo(executionStepInfo)
        .dataLoaderRegistry(dataLoaderRegistry)
        .fieldType(outputType)
        .selectionSet(mock(DataFetchingFieldSelectionSet.class))
        .fieldDefinition(graphQlFieldDefinitionMock)
        .operationDefinition(newOperationDefinition().operation(operation)
            .build())
        .source(source)
        .localContext(LocalDataFetcherContext.builder()
            .keyConditionFn((s, stringObjectMap) -> keyCondition)
            .build())
        .build();
  }

  private GraphQLOutputType createBreweryType() {
    return GraphQLObjectType.newObject()
        .name("Brewery")
        .build();
  }

  @Builder
  @Getter
  @EqualsAndHashCode
  private static class TestKeyCondition implements KeyCondition {
    private final Map<String, Object> valueMap;
  }
}
