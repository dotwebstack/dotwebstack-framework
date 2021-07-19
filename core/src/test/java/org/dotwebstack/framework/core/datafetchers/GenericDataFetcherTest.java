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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.core.InternalServerErrorException;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.Feature;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.paging.PagingDataFetcherContext;
import org.dotwebstack.framework.core.query.RequestFactory;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.ScalarField;
import org.dotwebstack.framework.core.query.model.origin.Origin;
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

  @Mock
  private RequestFactory requestFactory;

  private GenericDataFetcher genericDataFetcher;

  private DataLoaderRegistry dataLoaderRegistry;

  @BeforeEach
  void doBeforeEach() {
    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("Brewery", typeConfiguration));

    when(graphQlFieldDefinitionMock.getName()).thenReturn("brewery");

    dataLoaderRegistry = new DataLoaderRegistry();

    genericDataFetcher = new GenericDataFetcher(dotWebStackConfiguration, List.of(backendDataLoader), requestFactory);
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsFuture_forLoadSingleQueryOperation() throws Exception {
    Map<String, Object> data = Map.of("identifier", "id-1");
    var outputType = createBreweryType();
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    when(backendDataLoader.loadSingle(any(), any())).thenReturn(Mono.just(data));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);

    var future = genericDataFetcher.get(dataFetchingEnvironment);

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
  void get_returnsFuture_forLoadSingleQueryOperation_WhenUsingObjectQueryApproach() throws Exception {
    Map<String, Object> data = Map.of("identifier", "id-1");
    var outputType = createBreweryType();
    var objectQuery = createObjectQuery();
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    when(backendDataLoader.loadSingleRequest(any())).thenReturn(Mono.just(data));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.useRequestApproach()).thenReturn(true);
    when(requestFactory.createObjectRequest(typeConfiguration, dataFetchingEnvironment)).thenReturn(objectQuery);

    var future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    DataFetcherResult<Map<String, Object>> result = (DataFetcherResult<Map<String, Object>>) ((Future<?>) future).get();

    assertThat(Optional.of(result)
        .map(DataFetcherResult::getData)
        .orElseThrow()
        .entrySet(), equalTo(data.entrySet()));

    verify(backendDataLoader).loadSingleRequest(any(ObjectRequest.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsFuture_ForLoadManyQueryOperation() throws Exception {
    List<Map<String, Object>> data = List.of(Map.of("identifier", "id-1"), Map.of("identifier", "id-2"));
    var outputType = GraphQLList.list(createBreweryType());
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadMany(any(), any())).thenReturn(Flux.fromIterable(data));

    var future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    List<DataFetcherResult<Map<String, Object>>> result =
        (List<DataFetcherResult<Map<String, Object>>>) ((Future<?>) future).get();

    assertThat(result.stream()
        .map(DataFetcherResult::getData)
        .collect(Collectors.toList()), equalTo(data));

    verify(backendDataLoader).loadMany(isNull(), any(LoadEnvironment.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsFuture_ForLoadManyQueryOperation_WhenUsingObjectQueryApproach() throws Exception {
    List<Map<String, Object>> data = List.of(Map.of("identifier", "id-1"), Map.of("identifier", "id-2"));
    var outputType = GraphQLList.list(createBreweryType());
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);
    var collectionQuery = CollectionRequest.builder()
        .objectRequest(createObjectQuery())
        .build();

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadManyRequest(any(), any())).thenReturn(Flux.fromIterable(data));
    when(backendDataLoader.useRequestApproach()).thenReturn(true);
    when(requestFactory.createCollectionRequest(typeConfiguration, dataFetchingEnvironment))
        .thenReturn(collectionQuery);

    Object future = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(future, instanceOf(Future.class));

    List<DataFetcherResult<Map<String, Object>>> result =
        (List<DataFetcherResult<Map<String, Object>>>) ((Future<?>) future).get();

    assertThat(result.stream()
        .map(DataFetcherResult::getData)
        .collect(Collectors.toList()), equalTo(data));

    verify(backendDataLoader).loadManyRequest(eq(null), any(CollectionRequest.class));
  }

  @Test
  void get_returnsFlux_forLoadManySubscriptionOperation() {
    var outputType = GraphQLList.list(createBreweryType());
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, SUBSCRIPTION);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadMany(any(), any())).thenReturn(Flux.empty());

    var result = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, instanceOf(Flux.class));

    verify(backendDataLoader).loadMany(isNull(), any(LoadEnvironment.class));
  }

  @Test
  void get_returnsFlux_forLoadManySubscriptionOperation_WhenUsingObjectQueryApproach() {
    var outputType = GraphQLList.list(createBreweryType());
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, SUBSCRIPTION);
    var collectionQuery = CollectionRequest.builder()
        .objectRequest(createObjectQuery())
        .build();

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadManyRequest(any(), any())).thenReturn(Flux.empty());
    when(backendDataLoader.useRequestApproach()).thenReturn(true);
    when(requestFactory.createCollectionRequest(typeConfiguration, dataFetchingEnvironment))
        .thenReturn(collectionQuery);

    var result = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, instanceOf(Flux.class));

    verify(backendDataLoader).loadManyRequest(eq(null), any(CollectionRequest.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsDatafetcherResult_ForEagerLoadedNestedQueryOperation() {
    Map<String, Object> data = Map.of("identifier", "id-1");
    var outputType = GraphQLList.list(createBreweryType());
    Map<String, Object> source = Map.of("brewery", data);
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, source);

    when(dataFetchingEnvironment.getExecutionStepInfo()
        .getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);

    DataFetcherResult<Map<String, Object>> result =
        (DataFetcherResult<Map<String, Object>>) genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, notNullValue());
    assertThat(result.getData()
        .entrySet(), equalTo(data.entrySet()));
  }

  @Test
  void get_returnsDatafetcherResult_ForBatchLoadManyQueryOperation() throws Exception {
    var keyConditionWithBeer = TestKeyCondition.builder()
        .valueMap(Map.of("brewery", "id-brewery-1"))
        .build();
    var keyConditionWithoutBeer = TestKeyCondition.builder()
        .valueMap(Map.of("brewery", "id-brewery-2"))
        .build();

    var outputType = GraphQLList.list(createBeersType());

    Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadManyResult = Flux.fromIterable(List.of(
        new KeyConditionGroupedFlux(keyConditionWithBeer, Flux.fromIterable(List.of(Map.of("id", "id-1")))),
        new KeyConditionGroupedFlux(keyConditionWithoutBeer, Flux.fromIterable(List.of(GenericDataFetcher.NULL_MAP)))));

    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), keyConditionWithBeer);

    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("Beers", typeConfiguration));
    when(backendDataLoader.batchLoadMany(any(), any())).thenReturn(batchLoadManyResult);
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(executionStepInfo.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.parse("/my/beers"));
    when(executionStepInfo.getUnwrappedNonNullType()).thenReturn(outputType);

    final Future<?> futureWithBeer = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), keyConditionWithoutBeer);
    final Future<?> futureWithoutBeer = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    DataLoader<?, ?> dataLoader = dataFetchingEnvironment.getDataLoader("my/beers");

    dataLoader.dispatch();

    assertBatchLoadManyDataloaderResult(futureWithBeer, futureWithoutBeer);
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_returnsDatafetcherResult_ForLoadManyRequestOperation_whenUsingPaging() throws Exception {
    when(dotWebStackConfiguration.isFeatureEnabled(Feature.PAGING)).thenReturn(true);

    var keyConditionWithBeer = TestKeyCondition.builder()
        .valueMap(Map.of("brewery", "id-brewery-1"))
        .build();

    var outputType = GraphQLList.list(createBeersType());

    Flux<Map<String, Object>> loadManyRequestResult = Flux.fromIterable(List.of(Map.of("id", "id-1")));

    var parentLocalContext = LocalDataFetcherContext.builder()
        .keyConditionFn((s, stringObjectMap) -> keyConditionWithBeer)
        .build();

    PagingDataFetcherContext pagingDataFetcherContext = PagingDataFetcherContext.builder()
        .parentLocalContext(parentLocalContext)
        .first(10)
        .offset(0)
        .parentSource(Map.of("name", "Brewery X", "identifier_brewery", "id-brewery-1"))
        .build();

    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), pagingDataFetcherContext);

    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("Beers", typeConfiguration));
    when(backendDataLoader.loadManyRequest(eq(keyConditionWithBeer), any())).thenReturn(loadManyRequestResult);
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);

    when(executionStepInfo.getUnwrappedNonNullType()).thenReturn(outputType);
    ExecutionStepInfo parentExecutionStepInfo = mock(ExecutionStepInfo.class);
    when(parentExecutionStepInfo.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(executionStepInfo.getParent()).thenReturn(parentExecutionStepInfo);

    final Future<?> futureWithBeer = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    List<DataFetcherResult<Map<String, Object>>> futureWithBeerResult =
        (List<DataFetcherResult<Map<String, Object>>>) futureWithBeer.get();
    assertThat(futureWithBeerResult.size(), is(1));
    assertThat(futureWithBeerResult.get(0)
        .getData()
        .get("id"), is("id-1"));
  }

  @Test
  void get_returnsDatafetcherResult_ForBatchLoadManyQueryOperation_WhenUsingObjectQueryApproach() throws Exception {
    var keyConditionWithBeer = TestKeyCondition.builder()
        .valueMap(Map.of("brewery", "id-brewery-1"))
        .build();
    var keyConditionWithoutBeer = TestKeyCondition.builder()
        .valueMap(Map.of("brewery", "id-brewery-2"))
        .build();

    var collectionQuery = CollectionRequest.builder()
        .objectRequest(createObjectQuery())
        .build();

    Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadManyResult = Flux.fromIterable(List.of(
        new KeyConditionGroupedFlux(keyConditionWithBeer, Flux.fromIterable(List.of(Map.of("id", "id-1")))),
        new KeyConditionGroupedFlux(keyConditionWithoutBeer, Flux.fromIterable(List.of(GenericDataFetcher.NULL_MAP)))));

    var outputType = GraphQLList.list(GraphQLObjectType.newObject()
        .name("Beers")
        .build());

    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), keyConditionWithBeer);

    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("Beers", typeConfiguration));
    when(backendDataLoader.batchLoadManyRequest(any(), any())).thenReturn(batchLoadManyResult);
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.useRequestApproach()).thenReturn(true);
    when(requestFactory.createCollectionRequest(typeConfiguration, dataFetchingEnvironment))
        .thenReturn(collectionQuery);
    when(executionStepInfo.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.parse("/my/beers"));
    when(executionStepInfo.getUnwrappedNonNullType()).thenReturn(outputType);

    final Future<?> futureWithBeer = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), keyConditionWithoutBeer);
    final Future<?> futureWithoutBeer = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    DataLoader<?, ?> dataLoader = dataFetchingEnvironment.getDataLoader("my/beers");

    dataLoader.dispatch();

    assertBatchLoadManyDataloaderResult(futureWithBeer, futureWithoutBeer);
  }

  @Test
  void get_returnsDatafetcherResult_ForBatchLoadSingleQueryOperation() throws Exception {
    var outputType = createBreweryType();

    var breweryOneKey = TestKeyCondition.builder()
        .valueMap(Map.of("id", "id-1"))
        .build();
    Map<String, Object> breweryOneData = Map.of("id", "id-1");

    var breweryTwoKey = TestKeyCondition.builder()
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

    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), breweryOneKey);
    final Future<?> breweryOneFuture = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), breweryTwoKey);
    final Future<?> breweryTwoFuture = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    DataLoader<?, ?> dataLoader = dataFetchingEnvironment.getDataLoader("my/brewery");

    dataLoader.dispatch();

    assertBatchLoadSingleDataloaderResult(breweryOneFuture, breweryTwoFuture);
  }

  @Test
  void get_returnsDatafetcherResult_ForBatchLoadSingleQueryOperation_WhenUsingObjectQueryApproach() throws Exception {
    var outputType = createBreweryType();
    var objectQuery = createObjectQuery();

    KeyCondition breweryOneKey = TestKeyCondition.builder()
        .valueMap(Map.of("id", "id-1"))
        .build();
    Map<String, Object> breweryOneData = Map.of("id", "id-1");

    var breweryTwoKey = TestKeyCondition.builder()
        .valueMap(Map.of("id", "id-2"))
        .build();
    Map<String, Object> breweryTwoData = Map.of("id", "id-2");

    List<Tuple2<KeyCondition, Map<String, Object>>> breweries =
        List.of(Tuples.of(breweryOneKey, breweryOneData), Tuples.of(breweryTwoKey, breweryTwoData));

    DataFetchingEnvironment dataFetchingEnvironment =
        createDataFetchingEnvironment(outputType, QUERY, Map.of(), breweryOneKey);

    when(backendDataLoader.batchLoadSingleRequest(any())).thenReturn(Flux.fromIterable(breweries));
    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.useRequestApproach()).thenReturn(true);
    when(requestFactory.createObjectRequest(typeConfiguration, dataFetchingEnvironment)).thenReturn(objectQuery);
    when(executionStepInfo.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.parse("/my/brewery"));
    when(executionStepInfo.getUnwrappedNonNullType()).thenReturn(outputType);

    final Future<?> breweryOneFuture = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY, Map.of(), breweryTwoKey);
    final Future<?> breweryTwoFuture = (Future<?>) genericDataFetcher.get(dataFetchingEnvironment);

    DataLoader<?, ?> dataLoader = dataFetchingEnvironment.getDataLoader("my/brewery");

    dataLoader.dispatch();

    assertBatchLoadSingleDataloaderResult(breweryOneFuture, breweryTwoFuture);
  }

  @Test
  void get_throwsException_ForLoadManyQueryOperation() {
    var outputType = GraphQLList.list(createBreweryType());
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadMany(any(), any())).thenReturn(Flux.error(new RuntimeException("Query error!")));

    var result = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, instanceOf(Future.class));
    var executionException = assertThrows(ExecutionException.class, ((Future<?>) result)::get);
    assertThat(executionException.getCause(), instanceOf(InternalServerErrorException.class));
  }

  @Test
  void get_throwsException_ForLoadSingleQueryOperation() {
    var outputType = createBreweryType();
    var dataFetchingEnvironment = createDataFetchingEnvironment(outputType, QUERY);

    when(backendDataLoader.supports(typeConfiguration)).thenReturn(true);
    when(backendDataLoader.loadSingle(any(), any())).thenReturn(Mono.error(new RuntimeException("Query error!")));

    var result = genericDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, instanceOf(Future.class));
    var executionException = assertThrows(ExecutionException.class, ((Future<?>) result)::get);
    assertThat(executionException.getCause(), instanceOf(InternalServerErrorException.class));
  }

  private ObjectRequest createObjectQuery() {
    var fieldConfig = new TestFieldConfiguration();
    fieldConfig.setScalarField(true);
    fieldConfig.setName("identifier");

    return ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(List.of(createScalarField(fieldConfig)))
        .build();
  }

  private ScalarField createScalarField(FieldConfiguration fieldConfiguration) {
    return ScalarField.builder()
        .field(fieldConfiguration)
        .origins(Sets.newHashSet(Origin.requested()))
        .build();
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
    LocalDataFetcherContext localDataFetcherContext = LocalDataFetcherContext.builder()
        .keyConditionFn((s, stringObjectMap) -> keyCondition)
        .build();

    return createDataFetchingEnvironment(outputType, operation, source, localDataFetcherContext);
  }

  private DataFetchingEnvironment createDataFetchingEnvironment(GraphQLOutputType outputType,
      OperationDefinition.Operation operation, Object source, Object localDataFetcherContext) {
    return newDataFetchingEnvironment().executionStepInfo(executionStepInfo)
        .dataLoaderRegistry(dataLoaderRegistry)
        .fieldType(outputType)
        .selectionSet(mock(DataFetchingFieldSelectionSet.class))
        .fieldDefinition(graphQlFieldDefinitionMock)
        .operationDefinition(newOperationDefinition().operation(operation)
            .build())
        .source(source)
        .localContext(localDataFetcherContext)
        .build();
  }

  private GraphQLOutputType createBeersType() {
    return GraphQLObjectType.newObject()
        .name("Beers")
        .build();
  }

  private GraphQLOutputType createBreweryType() {
    return GraphQLObjectType.newObject()
        .name("Brewery")
        .build();
  }

  @SuppressWarnings("unchecked")
  private void assertBatchLoadManyDataloaderResult(Future<?> futureWithBeer, Future<?> futureWithoutBeer)
      throws InterruptedException, java.util.concurrent.ExecutionException {
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

  @SuppressWarnings("unchecked")
  private void assertBatchLoadSingleDataloaderResult(Future<?> breweryOneFuture, Future<?> breweryTwoFuture)
      throws InterruptedException, java.util.concurrent.ExecutionException {
    DataFetcherResult<Map<String, Object>> futureResult =
        (DataFetcherResult<Map<String, Object>>) breweryOneFuture.get();
    assertThat(futureResult.getData()
        .get("id"), is("id-1"));

    futureResult = (DataFetcherResult<Map<String, Object>>) breweryTwoFuture.get();
    assertThat(futureResult.getData()
        .get("id"), is("id-2"));
  }

  @Builder
  @Getter
  @EqualsAndHashCode
  private static class TestKeyCondition implements KeyCondition {
    private final Map<String, Object> valueMap;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  static class TestFieldConfiguration extends AbstractFieldConfiguration {
    private boolean isScalarField = false;

    private boolean isObjectField = false;

    private boolean isNestedObjectField = false;

    private boolean isAggregateField = false;

    private TypeConfiguration<?> typeConfiguration;

    @Override
    public boolean isScalarField() {
      return isScalarField;
    }

    @Override
    public boolean isObjectField() {
      return isObjectField;
    }

    @Override
    public boolean isNestedObjectField() {
      return isNestedObjectField;
    }

    @Override
    public boolean isAggregateField() {
      return isAggregateField;
    }
  }
}
