package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.hamcrest.CoreMatchers;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
class QueryTest {

  @Mock
  private DatabaseClient databaseClient;

  @Mock
  private SelectQuery<Record> selectQuery;

  @Mock
  private AliasManager aliasManager;

  @Mock
  private RequestContext requestContext;

  @Test
  void createSelect_initQuery_forObjectRequest() {
    ObjectRequest objectRequest = initObjectRequest();

    var result = new Query(objectRequest, requestContext);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Query);
  }

  @Test
  void createSelect_initQuery_forCollectionRequest() {
    Map<String, Object> source = new HashMap<>();
    source.put("a", "bbb");
    RequestContext requestContext = RequestContext.builder()
        .objectField(mock(PostgresObjectField.class))
        .source(source)
        .build();

    ObjectRequest objectRequest = initObjectRequest();

    CollectionRequest request = CollectionRequest.builder()
        .objectRequest(objectRequest)
        .sortCriterias(List.of())
        .filterCriterias(List.of())
        .build();

    var result = new Query(request, requestContext);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Query);
  }

  private ObjectRequest initObjectRequest() {
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    when(objectType.getTable()).thenReturn("anyTable");
    Map<FieldRequest, ObjectRequest> objectFields = Map.of();

    Map<String, Object> mapValues = Map.of("a", "b");
    ContextCriteria contextCriteria = mock(ContextCriteria.class);
    when(contextCriteria.getValues()).thenReturn(mapValues);
    when(contextCriteria.getName()).thenReturn("Brewery");


    return ObjectRequest.builder()
        .objectType(objectType)
        .objectFields(objectFields)
        .contextCriteria(contextCriteria)
        .build();
  }

}
