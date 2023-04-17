package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryTest {

  @Mock
  private RequestContext requestContext;

  @Test
  void createSelect_initQuery_forObjectRequest() {
    SingleObjectRequest objectRequest = initObjectRequest();

    var result = new Query(objectRequest, requestContext);
    assertThat(result, CoreMatchers.is(notNullValue()));
  }

  @Test
  void createSelect_initQuery_forCollectionRequest() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(PostgresObjectField.class))
        .source(source)
        .build();

    SingleObjectRequest objectRequest = initObjectRequest();

    CollectionRequest request = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .sortCriterias(List.of())
        .build();

    var result = new Query(request, requestContext);
    assertThat(result, CoreMatchers.is(notNullValue()));
  }

  private SingleObjectRequest initObjectRequest() {
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    when(objectType.getTable()).thenReturn("anyTable");
    Map<FieldRequest, SingleObjectRequest> objectFields = Map.of();

    return SingleObjectRequest.builder()
        .objectType(objectType)
        .objectFields(objectFields)
        .build();
  }
}
