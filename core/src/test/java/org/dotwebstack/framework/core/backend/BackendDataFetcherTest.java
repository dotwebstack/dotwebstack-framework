package org.dotwebstack.framework.core.backend;

import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLScalarType.newScalar;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.OperationDefinition;
import graphql.scalar.GraphqlStringCoercing;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.core.backend.validator.GraphQlValidator;
import org.dotwebstack.framework.core.datafetchers.KeyGroupedFlux;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.BatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@ExtendWith(MockitoExtension.class)
class BackendDataFetcherTest {

  @Mock
  private BackendLoader backendLoader;

  @Mock
  private BackendRequestFactory requestFactory;

  @Mock
  private BackendExecutionStepInfo backendExecutionStepInfo;

  @Mock
  private DataFetchingEnvironment environment;

  @Mock
  private List<GraphQlValidator> graphQlValidators;

  @InjectMocks
  private BackendDataFetcher backendDataFetcher;

  @Test
  void get_returnsObject_ifDataWasEagerLoaded() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    when(environment.getSource()).thenReturn(source);

    mockExecutionStepInfo("a", "a");

    var result = backendDataFetcher.get(environment);
    assertThat(result, is("bbb"));
  }

  @Test
  void get_returnsObject_ifDataWasEagerLoadedWithAlias() {
    Map<String, Object> source = new HashMap<>();
    source.put("a.alias", "bbb");
    when(environment.getSource()).thenReturn(source);

    mockExecutionStepInfo("a", "alias");

    var result = backendDataFetcher.get(environment);
    assertThat(result, is("bbb"));
  }

  @Test
  void get_throwsException_forBatchQueryMissingKey() {
    mockExecutionStepInfo("itemsBatchQuery", "itemsBatchQuery");
    mockOperationDefinition(OperationDefinition.Operation.QUERY);

    var objectRequest = ObjectRequest.builder()
        .build();

    when(requestFactory.createObjectRequest(any(ExecutionStepInfo.class), isNull())).thenReturn(objectRequest);

    var type = GraphQLList.list(newObject().name("Item")
        .build());

    var fieldDefinition = createFieldDefinitionForBatchKeyQuery(type);

    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(environment.getFieldType()).thenReturn(fieldDefinition.getType());
    when(environment.getArguments()).thenReturn(Map.of("identifier", List.of()));

    var thrown = assertThrows(IllegalArgumentException.class, () -> backendDataFetcher.get(environment));

    assertThat(thrown.getMessage(), equalTo("At least one batch key must be provided"));
  }

  @Test
  void get_throwsException_forBatchQueryMaxKeys() {
    mockExecutionStepInfo("itemsBatchQuery", "itemsBatchQuery");
    mockOperationDefinition(OperationDefinition.Operation.QUERY);

    var objectRequest = ObjectRequest.builder()
        .build();

    when(requestFactory.createObjectRequest(any(ExecutionStepInfo.class), isNull())).thenReturn(objectRequest);

    var type = GraphQLList.list(newObject().name("Item")
        .build());

    var fieldDefinition = createFieldDefinitionForBatchKeyQuery(type);

    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(environment.getFieldType()).thenReturn(fieldDefinition.getType());

    var keys = new ArrayList<>();
    for (int i = 1; i <= 120; i++) {
      keys.add(String.format("id-%d", i));
    }

    when(environment.getArguments()).thenReturn(Map.of("identifier", keys));

    var thrown = assertThrows(IllegalArgumentException.class, () -> backendDataFetcher.get(environment));

    assertThat(thrown.getMessage(), equalTo("Got 120 batch keys but a maximum of 100 batch keys is allowed!"));
  }

  @Test
  void get_returnsList_forBatchSingleQueryWithKeys() {
    mockExecutionStepInfo("itemsBatchQuery", "itemsBatchQuery");
    mockOperationDefinition(OperationDefinition.Operation.QUERY);

    var objectRequest = ObjectRequest.builder()
        .build();

    Flux<Tuple2<Map<String, Object>, Map<String, Object>>> fluxResult =
        Flux.fromIterable(List.of(Tuples.of(Map.of("identifier", "id-1"), Map.of("name", "foo")),
            Tuples.of(Map.of("identifier", "id-2"), Map.of("name", "bar"))));

    when(backendLoader.batchLoadSingle(ArgumentMatchers.any(BatchRequest.class), ArgumentMatchers.isNull()))
        .thenReturn(fluxResult);

    when(requestFactory.createObjectRequest(any(ExecutionStepInfo.class), isNull())).thenReturn(objectRequest);

    var type = GraphQLList.list(newObject().name("Item")
        .build());

    var fieldDefinition = createFieldDefinitionForBatchKeyQuery(type);

    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(environment.getFieldType()).thenReturn(fieldDefinition.getType());
    when(environment.getArguments()).thenReturn(Map.of("identifier", List.of("id-1", "id-2")));

    var result = backendDataFetcher.get(environment);

    assertThat(result, notNullValue());
    assertThat(result, equalTo(List.of(Map.of("name", "foo"), Map.of("name", "bar"))));
  }

  @Test
  void get_returnsList_forBatchManyQueryWithKeys() {
    mockExecutionStepInfo("itemsBatchQuery", "itemsBatchQuery");
    mockOperationDefinition(OperationDefinition.Operation.QUERY);

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(ObjectRequest.builder()
            .build())
        .build();

    Flux<GroupedFlux<Map<String, Object>, Map<String, Object>>> fluxResult = Flux.fromIterable(List.of(
        new KeyGroupedFlux(Map.of("identifier", "id-1"),
            Flux.fromIterable(List.of(Map.of("name", "foo", "version", 1), Map.of("name", "foo", "version", 2)))),
        new KeyGroupedFlux(Map.of("identifier", "id-2"),
            Flux.fromIterable(List.of(Map.of("name", "bar", "version", 1), Map.of("name", "bar", "version", 2))))));

    when(backendLoader.batchLoadMany(ArgumentMatchers.any(CollectionBatchRequest.class), ArgumentMatchers.isNull()))
        .thenReturn(fluxResult);

    when(requestFactory.createCollectionRequest(any(ExecutionStepInfo.class), isNull())).thenReturn(collectionRequest);

    var type = GraphQLList.list(GraphQLList.list(newObject().name("Item")
        .build()));

    var fieldDefinition = createFieldDefinitionForBatchKeyQuery(type);

    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(environment.getFieldType()).thenReturn(fieldDefinition.getType());
    when(environment.getArguments()).thenReturn(Map.of("identifier", List.of("id-1", "id-2")));

    var result = backendDataFetcher.get(environment);

    assertThat(result, notNullValue());
    assertThat(result,
        equalTo(List.of(List.of(Map.of("name", "foo", "version", 1), Map.of("name", "foo", "version", 2)),
            List.of(Map.of("name", "bar", "version", 1), Map.of("name", "bar", "version", 2)))));
  }

  @Test
  @Disabled("fix me")
  void get_returnsCompletableFuture_ifNotSubscription_ListTypeTrue_and_JoinCondition() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");

    Map<String, Object> condition = new HashMap<>();
    condition.put("b", "ccc");

    var joinCondition = JoinCondition.builder()
        .key(condition)
        .build();

    source.put("$join:aaa", joinCondition);
    graphql.language.Field fieldMock = new Field("aaa");
    var requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();

    var executionStepInfoMock = mockExecutionStepInfoWithResultPath("fff", "fff");

    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(source);
    lenient().when(environment.getField())
        .thenReturn(fieldMock);
    GraphQLList listType = mock(GraphQLList.class);
    when(environment.getFieldType()).thenReturn(listType);

    mockOperationDefinition(OperationDefinition.Operation.QUERY);

    when(environment.getDataLoaderRegistry()).thenReturn(new DataLoaderRegistry());

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(ObjectRequest.builder()
            .build())
        .build();

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSet);

    lenient().when(requestFactory.createCollectionRequest(eq(executionStepInfoMock), eq(selectionSet)))
        .thenReturn(collectionRequest);

    var result = backendDataFetcher.get(environment);

    assertThat(result, notNullValue());
    assertTrue(result instanceof CompletableFuture);
    verify(requestFactory, times(2)).createCollectionRequest(any(ExecutionStepInfo.class),
        any(DataFetchingFieldSelectionSet.class));
    verify(requestFactory).createRequestContext(any(DataFetchingEnvironment.class));
  }

  @Test
  void get_returnsFluxList_ifSourceNull_NotSubscription_and_ListTypeTrue() {
    var executionStepInfoMock = mockExecutionStepInfoWithResultPath("fff", "fff");

    var requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();

    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);
    graphql.language.Field fieldMock = new Field("aaa");
    lenient().when(environment.getField())
        .thenReturn(fieldMock);

    GraphQLList listType = mock(GraphQLList.class);
    when(environment.getFieldType()).thenReturn(listType);

    mockOperationDefinition(OperationDefinition.Operation.QUERY);

    CollectionRequest collectionRequestMock = CollectionRequest.builder()
        .objectRequest(ObjectRequest.builder()
            .build())
        .build();

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSet);
    lenient().when(requestFactory.createCollectionRequest(eq(executionStepInfoMock), eq(selectionSet)))
        .thenReturn(collectionRequestMock);
    Map<String, Object> resultMock = new HashMap<>();
    resultMock.put("aa", new String[] {"a", "b"});
    when(backendLoader.loadMany(any(CollectionRequest.class), any(RequestContext.class)))
        .thenReturn(Flux.just(resultMock));

    mockGraphQlFieldDefinition(Map.of());

    var result = ((CompletableFuture<?>) backendDataFetcher.get(environment)).join();

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof List);
    assertThat(((List<?>) result).get(0), is(resultMock));
    verify(requestFactory).createCollectionRequest(any(ExecutionStepInfo.class),
        any(DataFetchingFieldSelectionSet.class));
    verify(backendLoader).loadMany(any(CollectionRequest.class), any(RequestContext.class));
  }

  @Test
  void get_returnsFluxMap_ifSourceNull_SubscriptionTrue() {
    var executionStepInfoMock = mockExecutionStepInfoWithResultPath("bbb", "bbb");

    var requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();

    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);

    graphql.language.Field fieldMock = new Field("aaa");
    lenient().when(environment.getField())
        .thenReturn(fieldMock);

    mockOperationDefinition(OperationDefinition.Operation.SUBSCRIPTION);

    var collectionRequest = mock(CollectionRequest.class);

    var selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSet);
    lenient().when(requestFactory.createCollectionRequest(eq(executionStepInfoMock), eq(selectionSet)))
        .thenReturn(collectionRequest);

    Map<String, Object> data = new HashMap<>();
    data.put("aa", new String[] {"a", "b"});
    when(backendLoader.loadMany(any(CollectionRequest.class), any(RequestContext.class))).thenReturn(Flux.just(data));

    mockGraphQlFieldDefinition(Map.of());

    var result = ((Flux<?>) backendDataFetcher.get(environment)).blockFirst();

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Map);
    assertThat(((Map<?, ?>) result).get("aa"), is(data.get("aa")));

    verify(requestFactory).createCollectionRequest(any(ExecutionStepInfo.class),
        any(DataFetchingFieldSelectionSet.class));
    verify(backendLoader).loadMany(any(CollectionRequest.class), any(RequestContext.class));
  }

  @Test
  void get_returnsMonoMap_ifSourceNull_SubscriptionFalse_and_ListTypeFalse() {
    var executionStepInfoMock = mockExecutionStepInfo("fff", "fff");

    var requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();

    lenient().when(requestFactory.createRequestContext(environment))
        .thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);

    mockOperationDefinition(OperationDefinition.Operation.QUERY);

    var objectRequest = ObjectRequest.builder()
        .build();
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSet);
    lenient().when(requestFactory.createObjectRequest(eq(executionStepInfoMock), eq(selectionSet)))
        .thenReturn(objectRequest);

    Map<String, Object> data = new HashMap<>();
    data.put("aa", new String[] {"a", "b"});
    when(backendLoader.loadSingle(objectRequest, requestContext)).thenReturn(Mono.just(data));

    mockGraphQlFieldDefinition(Map.of());

    var result = ((CompletableFuture<?>) backendDataFetcher.get(environment)).join();

    assertTrue(result instanceof Map);
    assertThat(((Map<?, ?>) result).get("aa"), is(data.get("aa")));
    verify(requestFactory).createObjectRequest(any(ExecutionStepInfo.class), any(DataFetchingFieldSelectionSet.class));
    verify(backendLoader).loadSingle(any(ObjectRequest.class), any(RequestContext.class));
  }

  @Test
  void get_throwsException_ifBackendLoaderIsNull() {
    var dataFetcherWithoutBackendLoader =
        new BackendDataFetcher(null, requestFactory, backendExecutionStepInfo, graphQlValidators);

    mockExecutionStepInfo("a", "a");

    var thrown = assertThrows(IllegalStateException.class, () -> dataFetcherWithoutBackendLoader.get(environment));

    assertThat(thrown.getMessage(), equalTo("BackendLoader can't be null."));
  }

  private void mockOperationDefinition(OperationDefinition.Operation operation) {
    var operationDefinition = mock(OperationDefinition.class);

    when(operationDefinition.getOperation()).thenReturn(operation);

    when(environment.getOperationDefinition()).thenReturn(operationDefinition);
  }

  private ExecutionStepInfo mockExecutionStepInfo(String fieldName, String resultKey) {
    var field = mock(MergedField.class);
    when(field.getName()).thenReturn(fieldName);
    when(field.getResultKey()).thenReturn(resultKey);

    var executionStepInfo = mock(ExecutionStepInfo.class);
    when(executionStepInfo.getField()).thenReturn(field);

    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfo);

    lenient().when(environment.getExecutionStepInfo())
        .thenReturn(executionStepInfo);

    return executionStepInfo;
  }

  private ExecutionStepInfo mockExecutionStepInfoWithResultPath(String fieldName, String resultKey) {
    var executionStepInfoMock = mockExecutionStepInfo(fieldName, resultKey);

    var resultPath = ResultPath.rootPath()
        .segment("a");
    lenient().when(executionStepInfoMock.getPath())
        .thenReturn(resultPath);

    return executionStepInfoMock;
  }

  private void mockGraphQlFieldDefinition(Map<String, String> additionalData) {
    var graphQlFieldDefinition = mock(GraphQLFieldDefinition.class);
    when(environment.getFieldDefinition()).thenReturn(graphQlFieldDefinition);
    var fieldDefinition = mock(FieldDefinition.class);
    when(graphQlFieldDefinition.getDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getAdditionalData()).thenReturn(additionalData);
  }

  private GraphQLFieldDefinition createFieldDefinitionForBatchKeyQuery(GraphQLOutputType outputType) {
    return newFieldDefinition().name("itemsBatchQuery")
        .definition(FieldDefinition.newFieldDefinition()
            .additionalData(GraphQlConstants.IS_BATCH_KEY_QUERY, Boolean.TRUE.toString())
            .build())
        .type(outputType)
        .argument(newArgument().type(GraphQLList.list(newScalar().name("String")
            .coercing(new GraphqlStringCoercing())
            .build()))
            .definition(newInputValueDefinition().additionalData(GraphQlConstants.KEY_FIELD, Boolean.TRUE.toString())
                .build())
            .name("identifier")
            .build())
        .build();
  }
}
