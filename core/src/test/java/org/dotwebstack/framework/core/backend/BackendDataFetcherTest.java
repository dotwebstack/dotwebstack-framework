package org.dotwebstack.framework.core.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.core.backend.validator.GraphQlValidator;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
  private BackendDataFetcher dataFetcher;

  @Test
  void get_returnObject_ifDataWasEagerLoaded() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    when(environment.getSource()).thenReturn(source);

    var fieldMock = mock(MergedField.class);
    when(fieldMock.getName()).thenReturn("a");
    ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
    when(executionStepInfo.getField()).thenReturn(fieldMock);
    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfo);

    var result = dataFetcher.get(environment);
    assertThat(result, is("bbb"));
  }

  @Test
  @Disabled("fix me")
  void get_returnCompletableFuture_ifNotSubscription_ListTypeTrue_and_JoinCondition() {
    var fieldMock1 = mock(MergedField.class);
    when(fieldMock1.getName()).thenReturn("fff");
    ExecutionStepInfo executionStepInfoMock = mock(ExecutionStepInfo.class);
    when(executionStepInfoMock.getField()).thenReturn(fieldMock1);
    ResultPath resultPath = ResultPath.rootPath()
        .segment("a");
    when(executionStepInfoMock.getPath()).thenReturn(resultPath);

    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfoMock);
    when(environment.getExecutionStepInfo()).thenReturn(executionStepInfoMock);

    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    Map<String, Object> condition = new HashMap<>();
    condition.put("b", "ccc");
    JoinCondition joinCondition = JoinCondition.builder()
        .key(condition)
        .build();
    source.put("$join:aaa", joinCondition);
    graphql.language.Field fieldMock = new Field("aaa");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();

    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(source);
    lenient().when(environment.getField())
        .thenReturn(fieldMock);
    GraphQLList listTypeMock = mock(GraphQLList.class);
    when(environment.getFieldType()).thenReturn(listTypeMock);

    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation())
        .thenReturn(OperationDefinition.Operation.MUTATION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);

    when(environment.getDataLoaderRegistry()).thenReturn(new DataLoaderRegistry());

    CollectionRequest collectionRequestMock = mock(CollectionRequest.class);
    DataFetchingFieldSelectionSet selectionSetMock = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSetMock);

    lenient().when(requestFactory.createCollectionRequest(eq(executionStepInfoMock), eq(selectionSetMock)))
        .thenReturn(collectionRequestMock);

    var result = dataFetcher.get(environment);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof CompletableFuture);
    verify(requestFactory, times(2)).createCollectionRequest(any(ExecutionStepInfo.class),
        any(DataFetchingFieldSelectionSet.class));
    verify(requestFactory).createRequestContext(any(DataFetchingEnvironment.class));
  }

  @Test
  void get_returnFluxList_ifSourceNull_NotSubscription_and_ListTypeTrue() {
    var fieldMock1 = mock(MergedField.class);
    when(fieldMock1.getName()).thenReturn("fff");
    ExecutionStepInfo executionStepInfoMock = mock(ExecutionStepInfo.class);
    when(executionStepInfoMock.getField()).thenReturn(fieldMock1);
    ResultPath resultPath = ResultPath.rootPath()
        .segment("a");
    lenient().when(executionStepInfoMock.getPath())
        .thenReturn(resultPath);

    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfoMock);
    lenient().when(environment.getExecutionStepInfo())
        .thenReturn(executionStepInfoMock);

    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();

    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);
    graphql.language.Field fieldMock = new Field("aaa");
    lenient().when(environment.getField())
        .thenReturn(fieldMock);

    GraphQLList listTypeMock = mock(GraphQLList.class);
    when(environment.getFieldType()).thenReturn(listTypeMock);
    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation())
        .thenReturn(OperationDefinition.Operation.MUTATION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);

    CollectionRequest collectionRequestMock = mock(CollectionRequest.class);
    DataFetchingFieldSelectionSet selectionSetMock = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSetMock);
    lenient().when(requestFactory.createCollectionRequest(eq(executionStepInfoMock), eq(selectionSetMock)))
        .thenReturn(collectionRequestMock);
    Map<String, Object> resultMock = new HashMap<>();
    resultMock.put("aa", new String[] {"a", "b"});
    when(backendLoader.loadMany(any(CollectionRequest.class), any(RequestContext.class)))
        .thenReturn(Flux.just(resultMock));

    var result = ((CompletableFuture) dataFetcher.get(environment)).join();

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof List);
    assertThat(((List<?>) result).get(0), is(resultMock));
    verify(requestFactory).createCollectionRequest(any(ExecutionStepInfo.class),
        any(DataFetchingFieldSelectionSet.class));
    verify(backendLoader).loadMany(any(CollectionRequest.class), any(RequestContext.class));
  }

  @Test
  void get_returnFluxMap_ifSourceNull_SubscriptionTrue() {
    var fieldMock1 = mock(MergedField.class);
    when(fieldMock1.getName()).thenReturn("fff");
    ExecutionStepInfo executionStepInfoMock = mock(ExecutionStepInfo.class);
    when(executionStepInfoMock.getField()).thenReturn(fieldMock1);
    ResultPath resultPath = ResultPath.rootPath()
        .segment("a");
    lenient().when(executionStepInfoMock.getPath())
        .thenReturn(resultPath);

    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfoMock);
    lenient().when(environment.getExecutionStepInfo())
        .thenReturn(executionStepInfoMock);

    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();

    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);
    graphql.language.Field fieldMock = new Field("aaa");
    lenient().when(environment.getField())
        .thenReturn(fieldMock);

    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation())
        .thenReturn(OperationDefinition.Operation.SUBSCRIPTION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);

    CollectionRequest collectionRequestMock = mock(CollectionRequest.class);
    DataFetchingFieldSelectionSet selectionSetMock = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSetMock);
    lenient().when(requestFactory.createCollectionRequest(eq(executionStepInfoMock), eq(selectionSetMock)))
        .thenReturn(collectionRequestMock);
    Map<String, Object> resultMock = new HashMap<>();
    resultMock.put("aa", new String[] {"a", "b"});
    when(backendLoader.loadMany(any(CollectionRequest.class), any(RequestContext.class)))
        .thenReturn(Flux.just(resultMock));

    var result = ((Flux) dataFetcher.get(environment)).blockFirst();

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Map);
    assertThat(((Map<?, ?>) result).get("aa"), is(resultMock.get("aa")));
    verify(requestFactory).createCollectionRequest(any(ExecutionStepInfo.class),
        any(DataFetchingFieldSelectionSet.class));
    verify(backendLoader).loadMany(any(CollectionRequest.class), any(RequestContext.class));
  }

  @Test
  void get_returnMonoMap_ifSourceNull_SubscriptionFalse_and_ListTypeFalse() {
    var fieldMock1 = mock(MergedField.class);
    when(fieldMock1.getName()).thenReturn("fff");
    ExecutionStepInfo executionStepInfoMock = mock(ExecutionStepInfo.class);
    when(executionStepInfoMock.getField()).thenReturn(fieldMock1);

    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfoMock);

    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();

    lenient().when(requestFactory.createRequestContext(environment))
        .thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);

    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation())
        .thenReturn(OperationDefinition.Operation.MUTATION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);

    ObjectRequest objectRequest = mock(ObjectRequest.class);
    DataFetchingFieldSelectionSet selectionSetMock = mock(DataFetchingFieldSelectionSet.class);
    when(environment.getSelectionSet()).thenReturn(selectionSetMock);
    lenient().when(requestFactory.createObjectRequest(eq(executionStepInfoMock), eq(selectionSetMock)))
        .thenReturn(objectRequest);

    Map<String, Object> resultMock = new HashMap<>();
    resultMock.put("aa", new String[] {"a", "b"});
    when(backendLoader.loadSingle(objectRequest, requestContext)).thenReturn(Mono.just(resultMock));

    var result = ((CompletableFuture) dataFetcher.get(environment)).join();

    assertTrue(result instanceof Map);
    assertThat(((Map<?, ?>) result).get("aa"), is(resultMock.get("aa")));
    verify(requestFactory).createObjectRequest(any(ExecutionStepInfo.class), any(DataFetchingFieldSelectionSet.class));
    verify(backendLoader).loadSingle(any(ObjectRequest.class), any(RequestContext.class));
  }

}
