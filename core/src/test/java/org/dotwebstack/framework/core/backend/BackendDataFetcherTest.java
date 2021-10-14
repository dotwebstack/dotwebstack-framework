package org.dotwebstack.framework.core.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;

import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.JoinCondition;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class BackendDataFetcherTest {
  
  @Mock
  private BackendLoader backendLoader;
  
  @Mock
  private BackendRequestFactory requestFactory;
  
  @Mock
  private DataFetchingEnvironment environment;
  
  @InjectMocks
  private BackendDataFetcher dataFetcher;

  @Test
  public void get_returnObject_ifDataWasEagerLoaded() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    graphql.language.Field fieldMock = new Field("a");
    
    when(environment.getSource()).thenReturn(source);
    when(environment.getField()).thenReturn(fieldMock);
    
    var result = dataFetcher.get(environment);
    assertThat(result, is("bbb"));
  }
  
  @Test
  public void get_returnCompletableFuture_ifNotSubscription_and_ListTypeTrue() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    Map<String, Object> condition = new HashMap<>();
    condition.put("b", "ccc");
    JoinCondition joinCondition = JoinCondition.builder().key(condition).build();
    source.put("$join:aaa", joinCondition);
    graphql.language.Field fieldMock = new Field("aaa");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();
  
    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(source);
    when(environment.getField()).thenReturn(fieldMock);
    GraphQLList listTypeMock = mock(GraphQLList.class);
    when(environment.getFieldType()).thenReturn(listTypeMock);
    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);
    ExecutionStepInfo executionStepInfoMock = mock(ExecutionStepInfo.class);
    ResultPath resultPath = ResultPath.rootPath().segment("a");
    when(executionStepInfoMock.getPath()).thenReturn(resultPath);
    when(environment.getExecutionStepInfo()).thenReturn(executionStepInfoMock);
    when(environment.getDataLoaderRegistry()).thenReturn(new DataLoaderRegistry());
    CollectionRequest collectionRequestMock = mock(CollectionRequest.class);
    when(requestFactory.createCollectionRequest(environment)).thenReturn(collectionRequestMock);
    
    var result = dataFetcher.get(environment);
  
    assertTrue(result instanceof CompletableFuture);
    verify(requestFactory, times(2)).createCollectionRequest(any(DataFetchingEnvironment.class));
    verify(requestFactory).createRequestContext(any(DataFetchingEnvironment.class));
  }
  
  @Test
  public void get_returnFluxList_ifSourceNull_NotSubscription_and_ListTypeTrue() {
    graphql.language.Field fieldMock = new Field("aaa");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();
  
    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);
    when(environment.getField()).thenReturn(fieldMock);
    
    GraphQLList listTypeMock = mock(GraphQLList.class);
    when(environment.getFieldType()).thenReturn(listTypeMock);
    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);
  
    CollectionRequest collectionRequestMock = mock(CollectionRequest.class);
    when(requestFactory.createCollectionRequest(environment)).thenReturn(collectionRequestMock);
    Map<String, Object> resultMock = new HashMap<>();
    resultMock.put("aa", new String[]{"a", "b"});
    when(backendLoader.loadMany(eq(collectionRequestMock), eq(requestContext))).thenReturn(Flux.just(resultMock));
    
    var result = ((CompletableFuture) dataFetcher.get(environment)).join();
  
    assertTrue(result instanceof List);
    assertThat(((List<?>) result).get(0), is(resultMock));
    verify(requestFactory).createCollectionRequest(any(DataFetchingEnvironment.class));
    verify(backendLoader).loadMany(any(CollectionRequest.class), any(RequestContext.class));
  }
  
  @Test
  public void get_returnFluxMap_ifSourceNull_SubscriptionTrue() {
    graphql.language.Field fieldMock = new Field("aaa");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();
  
    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);
    when(environment.getField()).thenReturn(fieldMock);
    
    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation()).thenReturn(OperationDefinition.Operation.SUBSCRIPTION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);
  
    CollectionRequest collectionRequestMock = mock(CollectionRequest.class);
    when(requestFactory.createCollectionRequest(environment)).thenReturn(collectionRequestMock);
    Map<String, Object> resultMock = new HashMap<>();
    resultMock.put("aa", new String[]{"a", "b"});
    when(backendLoader.loadMany(eq(collectionRequestMock), eq(requestContext))).thenReturn(Flux.just(resultMock));
    
    var result = ((Flux) dataFetcher.get(environment)).blockFirst();
  
    assertTrue(result instanceof Map);
    assertThat(((Map<?, ?>) result).get("aa"), is(resultMock.get("aa")));
  }
  
  @Test
  public void get_returnMonoMap_ifSourceNull_SubscriptionFalse_and_ListTypeFalse() {
    graphql.language.Field fieldMock = new Field("aaa");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(null)
        .build();
  
    when(requestFactory.createRequestContext(environment)).thenReturn(requestContext);
    when(environment.getSource()).thenReturn(null);
    when(environment.getField()).thenReturn(fieldMock);
    
    OperationDefinition operationDefinitionMock = mock(OperationDefinition.class);
    lenient().when(operationDefinitionMock.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
    when(environment.getOperationDefinition()).thenReturn(operationDefinitionMock);
  
    ObjectRequest objectRequest = mock(ObjectRequest.class);
    when(requestFactory.createObjectRequest(environment)).thenReturn(objectRequest);
    
    Map<String, Object> resultMock = new HashMap<>();
    resultMock.put("aa", new String[]{"a", "b"});
    when(backendLoader.loadSingle(eq(objectRequest), eq(requestContext))).thenReturn(Mono.just(resultMock));
    
    var result = ((CompletableFuture) dataFetcher.get(environment)).join();
  
    assertTrue(result instanceof Map);
    assertThat(((Map<?, ?>) result).get("aa"), is(resultMock.get("aa")));
    verify(requestFactory).createObjectRequest(any(DataFetchingEnvironment.class));
  }
  
}
