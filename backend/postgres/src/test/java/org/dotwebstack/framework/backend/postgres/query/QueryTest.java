package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.*;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueryTest {

  @Mock
  private RequestContext requestContext;

  @Test
  void createSelect_initQuery_forObjectRequest() {
    List<KeyCriteria> keyCriteria = List.of();
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    when(objectType.getTable()).thenReturn("anyTable");
    Map<FieldRequest, ObjectRequest> objectFields = Map.of();

    Map<String, Object> mapValues = Map.of("a", "b");
    ContextCriteria contextCriteria = mock(ContextCriteria.class);
    when(contextCriteria.getValues()).thenReturn(mapValues);
    when(contextCriteria.getName()).thenReturn("Brewery");
    ObjectRequest objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectFields(objectFields)
        .keyCriteria(keyCriteria)
        .contextCriteria(contextCriteria)
        .build();

    var result = new Query(objectRequest, requestContext);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Query);
  }

  @Test
  void createSelect_initQuery_forCollectionRequest() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(ObjectField.class))
        .source(source)
        .build();

    List<KeyCriteria> keyCriteria = List.of();
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    when(objectType.getTable()).thenReturn("anyTable");
    Map<FieldRequest, ObjectRequest> objectFields = Map.of();

    Map<String, Object> mapValues = Map.of("a", "b");
    ContextCriteria contextCriteria = mock(ContextCriteria.class);
    when(contextCriteria.getValues()).thenReturn(mapValues);
    when(contextCriteria.getName()).thenReturn("Brewery");
    ObjectRequest objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectFields(objectFields)
        .keyCriteria(keyCriteria)
        .contextCriteria(contextCriteria)
        .build();

    CollectionRequest request = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .sortCriterias(List.of())
        .filterCriterias(List.of())
        .build();

    var result = new Query(request, requestContext);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Query);
  }
}
