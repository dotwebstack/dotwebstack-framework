package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class PostgresBackendLoaderTest {

  @Mock
  private DatabaseClient databaseClient;

  private PostgresBackendLoader backendLoader;

  @BeforeEach
  void doBeforeEach() {
    backendLoader = new PostgresBackendLoader(databaseClient);
  }

  @Test
  void loadSingle_returnsMonoObject() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();

    FetchSpec fetchSpec = mock(FetchSpec.class);
    when(fetchSpec.all()).thenReturn(Flux.just(Collections.emptyMap()));
    DatabaseClient.GenericExecuteSpec anotherSpec = mock(DatabaseClient.GenericExecuteSpec.class);
    when(anotherSpec.fetch()).thenReturn(fetchSpec);
    DatabaseClient.GenericExecuteSpec spec = mock(DatabaseClient.GenericExecuteSpec.class);
    when(spec.bind(anyInt(), any())).thenReturn(anotherSpec);
    when(databaseClient.sql(anyString())).thenReturn(spec);

    ObjectRequest objectRequest = initObjectRequest();

    var res = backendLoader.loadSingle(objectRequest, requestContext);

    assertThat(res, CoreMatchers.is(notNullValue()));
    assertTrue(res instanceof Mono);
    res.doOnNext(result -> assertTrue(result.isEmpty()))
        .subscribe();
  }

  @Test
  void loadMany_returnsFluxObject() {
    FetchSpec fetchSpec = mock(FetchSpec.class);
    when(fetchSpec.all()).thenReturn(Flux.just(Collections.emptyMap()));
    DatabaseClient.GenericExecuteSpec anotherSpec = mock(DatabaseClient.GenericExecuteSpec.class);
    when(anotherSpec.fetch()).thenReturn(fetchSpec);
    DatabaseClient.GenericExecuteSpec spec = mock(DatabaseClient.GenericExecuteSpec.class);
    when(spec.bind(anyInt(), any())).thenReturn(anotherSpec);
    when(databaseClient.sql(anyString())).thenReturn(spec);

    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();

    ObjectRequest objectRequest = initObjectRequest();

    CollectionRequest request = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .sortCriterias(List.of())
        .filterCriterias(List.of())
        .build();

    var res = backendLoader.loadMany(request, requestContext);
    assertThat(res, CoreMatchers.is(notNullValue()));
    assertTrue(res instanceof Flux);
    res.doOnNext(result -> assertTrue(result.isEmpty()))
        .subscribe();
  }

  @Test
  @Disabled("fix me")
  void batchLoadMany_returnsFluxObject() {
    FetchSpec fetchSpec = mock(FetchSpec.class);
    when(fetchSpec.all()).thenReturn(Flux.just(Map.of("@@@", "ccc")));
    DatabaseClient.GenericExecuteSpec spec = mock(DatabaseClient.GenericExecuteSpec.class);
    when(spec.fetch()).thenReturn(fetchSpec);
    when(databaseClient.sql(anyString())).thenReturn(spec);

    PostgresObjectField objectFieldMock = mock(PostgresObjectField.class);
    when(objectFieldMock.getJoinTable()).thenReturn(mock(JoinTable.class));
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    RequestContext requestContext = RequestContext.builder()
        .objectField(objectFieldMock)
        .source(source)
        .build();

    ObjectRequest objectRequest = initObjectRequest();

    CollectionRequest collectionRequest = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .sortCriterias(List.of())
        .filterCriterias(List.of())
        .build();

    CollectionBatchRequest request = CollectionBatchRequest.builder()
        .collectionRequest(collectionRequest)
        .joinCriteria(JoinCriteria.builder()
            .build())
        .build();

    var res = backendLoader.batchLoadMany(request, requestContext);
    assertThat(res, CoreMatchers.is(notNullValue()));
    assertTrue(res instanceof Flux);
  }

  private ObjectRequest initObjectRequest() {
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    when(objectType.getTable()).thenReturn("anyTable");
    Map<FieldRequest, ObjectRequest> objectFields = Map.of();

    Map<String, Object> mapValues = Map.of("a", "bbb");
    ContextCriteria contextCriteria = mock(ContextCriteria.class);
    when(contextCriteria.getValues()).thenReturn(mapValues);
    when(contextCriteria.getName()).thenReturn("Brewery");

    List<KeyCriteria> keyCriteria = List.of();

    return ObjectRequest.builder()
        .objectType(objectType)
        .objectFields(objectFields)
        .keyCriteria(keyCriteria)
        .contextCriteria(contextCriteria)
        .build();

  }
}
