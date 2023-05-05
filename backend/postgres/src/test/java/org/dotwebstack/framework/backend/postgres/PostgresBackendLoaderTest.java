package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.query.Query;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.ContextField;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.CollectionBatchRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PostgresBackendLoaderTest {

  @Mock
  private PostgresClient postgresClient;

  private PostgresBackendLoader backendLoader;

  @BeforeEach
  void doBeforeEach() {
    backendLoader = new PostgresBackendLoader(postgresClient);
  }

  @Test
  void loadSingle_returnsMonoObject() {
    when(postgresClient.fetch(any(Query.class))).thenReturn(Flux.just(Collections.emptyMap()));
    Map<String, Object> source = Map.of("a", "bbb");

    var requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();

    var objectRequest = initObjectRequest();

    var result = backendLoader.loadSingle(objectRequest, requestContext);

    StepVerifier.create(result)
        .expectNext(Collections.emptyMap())
        .verifyComplete();
  }

  @Test
  void loadMany_returnsFluxObject() {
    when(postgresClient.fetch(any(Query.class))).thenReturn(Flux.just(Collections.emptyMap()));
    Map<String, Object> source = Map.of("a", "bbb");

    var requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();

    var objectRequest = initObjectRequest();

    var request = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .sortCriterias(List.of())
        .build();

    var res = backendLoader.loadMany(request, requestContext);
    assertThat(res, CoreMatchers.is(notNullValue()));
    res.doOnNext(result -> assertTrue(result.isEmpty()))
        .subscribe();
  }

  @Test
  @Disabled("fix me")
  void batchLoadMany_returnsFluxObject() {
    when(postgresClient.fetch(any(Query.class))).thenReturn(Flux.just(Map.of("@@@", "ccc")));
    PostgresObjectField objectFieldMock = mock(PostgresObjectField.class);
    when(objectFieldMock.getJoinTable()).thenReturn(mock(JoinTable.class));
    Map<String, Object> source = Map.of("a", "bbb");

    var requestContext = RequestContext.builder()
        .objectField(objectFieldMock)
        .source(source)
        .build();

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(initObjectRequest())
        .sortCriterias(List.of())
        .build();

    var request = CollectionBatchRequest.builder()
        .collectionRequest(collectionRequest)
        .joinCriteria(JoinCriteria.builder()
            .build())
        .build();

    var res = backendLoader.batchLoadMany(request, requestContext);
    assertThat(res, CoreMatchers.is(notNullValue()));
  }

  private SingleObjectRequest initObjectRequest() {
    var objectType = mock(PostgresObjectType.class);
    when(objectType.getTable()).thenReturn("anyTable");

    Map<String, Object> mapValues = Map.of("a", "bbb");
    var contextCriteria = mock(ContextCriteria.class);

    var context = mock(Context.class);
    when(context.getFields()).thenReturn(Map.of("a", mock(ContextField.class)));

    when(contextCriteria.getContext()).thenReturn(context);
    when(contextCriteria.getValues()).thenReturn(mapValues);
    when(contextCriteria.getName()).thenReturn("Brewery");

    Map<FieldRequest, SingleObjectRequest> objectFields = Map.of();

    return SingleObjectRequest.builder()
        .objectType(objectType)
        .objectFields(objectFields)
        .contextCriteria(contextCriteria)
        .build();
  }
}
