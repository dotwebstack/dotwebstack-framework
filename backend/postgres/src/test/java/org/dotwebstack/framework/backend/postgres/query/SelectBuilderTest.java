package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.JoinCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
class SelectBuilderTest {

  @Mock
  private DatabaseClient databaseClient;

  @Mock
  private RequestContext requestContext;

  @Mock
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @Mock
  private AliasManager aliasManager;

  @InjectMocks
  private SelectBuilder selectBuilder;

  private ObjectRequest.ObjectRequestBuilder objectRequestBuilder;

  private List<SortCriteria> sortCriteriaList;

  private Collection<FieldRequest> scalarFields;

  private CollectionRequest.CollectionRequestBuilder collectionRequestBuilder;

  @BeforeEach
  void setUp() {
    initObjectRequestBuilder();
  }

  @Test
  void build_throwsIllegalStateException_forFilterField() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField filterField = mock(PostgresObjectField.class);
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(filterField))
        .value(Map.of("a", "b"));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var exception =
        assertThrows(IllegalArgumentException.class, () -> selectBuilder.build(collectionRequestBuilder.build(), null));

    assertThat(exception.getMessage(), CoreMatchers.is("Unknown filter filterField 'a'"));
  }

  @Test
  void createFilterCondition_returnsCondition_forManyCriterias() {
    initSortCriteriaList(SortDirection.ASC);
    initCollectionRequest();

    PostgresObjectType targetType = mock(PostgresObjectType.class);
    when(targetType.getTable()).thenReturn("a");
    PostgresObjectField current2 = mock(PostgresObjectField.class);
    when(current2.getTargetType()).thenReturn(targetType);
    ObjectType objectType = mock(PostgresObjectType.class);
    PostgresObjectField field = mock(PostgresObjectField.class);
    when(field.getColumn()).thenReturn("b");
    when(objectType.getField(anyString())).thenReturn(Optional.ofNullable(field));
    when(current2.getObjectType()).thenReturn(objectType);

    JoinColumn joinColumn = mock(JoinColumn.class);
    when(joinColumn.getName()).thenReturn("a");
    when(joinColumn.getReferencedField()).thenReturn("a");

    PostgresObjectField mapped = mock(PostgresObjectField.class);
    when(mapped.getJoinColumns()).thenReturn(List.of(joinColumn));
    when(current2.getMappedByObjectField()).thenReturn(mapped);

    PostgresObjectField current1 = mock(PostgresObjectField.class);
    when(current1.getColumn()).thenReturn("b");

    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current2, current1))
        .value(Map.of("eq", "b", "lt", "a"));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .contains("select  as \nfrom anyTable_Brewery_ctx('b') as \nwhere exists (\n"
            + "  select 1\n  from a_Brewery_ctx('b') as \n  where (\n    \"b\" = \"a\"\n    and b < 'a'\n    and b = 'b'\n"
            + "  )\n)\norder by \"a\" asc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionEq() {
    initSortCriteriaList(SortDirection.ASC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("eq", "b"));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where a = 'b'\n" + "order by \"a\" asc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionLt() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("lt", "b"));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where a < 'b'\n" + "order by \"a\" desc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionLte() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("lte", "b"));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where a <= 'b'\n" + "order by \"a\" desc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionGt() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("gt", "b"));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where a > 'b'\n" + "order by \"a\" desc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionGte() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("gte", "b"));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where a >= 'b'\n" + "order by \"a\" desc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionIn() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("in", List.of("a", "b", "c")));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where a in (\n" + "  'a', 'b', 'c'\n" + ")\n" + "order by \"a\" desc"));
  }

  @Test
  void build_throwsException_forFilterConditionGeometry() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    when(current.getType()).thenReturn("Geometry");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("contains", Map.of("fromWKT", "c")));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var exception =
        assertThrows(IllegalArgumentException.class, () -> selectBuilder.build(collectionRequestBuilder.build(), null));

    assertThat(exception.getMessage(), CoreMatchers.is("The filter input WKT is invalid!"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionGeometryContains() {
    initSortCriteriaList(SortDirection.ASC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    when(current.getType()).thenReturn("Geometry");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("contains", Map.of("fromWKT", "POINT (5.979274334569982 52.21715768613606)")));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where (ST_Contains(a, cast('POINT (5.979274334569982 52.21715768613606)' as geometry)))\n"
            + "order by \"a\" asc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionGeometryWithin() {
    initSortCriteriaList(SortDirection.ASC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    when(current.getType()).thenReturn("Geometry");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("within", Map.of("fromWKT", "POINT (5.979274334569982 52.21715768613606)")));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where (ST_Within(cast('POINT (5.979274334569982 52.21715768613606)' as geometry), a))\n"
            + "order by \"a\" asc"));
  }

  @Test
  void build_returnsSelectQuery_forFilterConditionGeometryIntersects() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    when(current.getType()).thenReturn("Geometry");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("intersects", Map.of("fromWKT", "POINT (5.979274334569982 52.21715768613606)")));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .endsWith("where (ST_Intersects(a, cast('POINT (5.979274334569982 52.21715768613606)' as geometry)))\n"
            + "order by \"a\" desc"));
  }

  @Test
  void build_throwsException_forFilterConditionGeometryUnknown() {
    initSortCriteriaList(SortDirection.DESC);
    initCollectionRequest();

    PostgresObjectField current = mock(PostgresObjectField.class);
    when(current.getColumn()).thenReturn("a");
    when(current.getType()).thenReturn("Geometry");
    FilterCriteria.FilterCriteriaBuilder filterCriteria = FilterCriteria.builder()
        .fieldPath(List.of(current))
        .value(Map.of("unknown", Map.of("fromWKT", "POINT (5.979274334569982 52.21715768613606)")));
    List<FilterCriteria> filterCriteriaList = List.of(filterCriteria.build());

    collectionRequestBuilder.filterCriterias(filterCriteriaList);

    var exception =
        assertThrows(IllegalArgumentException.class, () -> selectBuilder.build(collectionRequestBuilder.build(), null));

    assertThat(exception.getMessage(), CoreMatchers.is("Unsupported geometry filter operation"));
  }

  @Test
  void build_returnsSelectQuery_forJoinCriteriaNull() {
    initCollectionRequest();
    collectionRequestBuilder.sortCriterias(List.of())
        .filterCriterias(List.of());

    var result = selectBuilder.build(collectionRequestBuilder.build(), null);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .contains("from anyTable_Brewery_ctx('b') as"));
  }

  @Test
  void build_returnsSelectQuery_forJoinCriteria() {
    initSortCriteriaList(SortDirection.ASC);
    initCollectionRequest();
    collectionRequestBuilder.filterCriterias(List.of());

    JoinColumn joinColumn = mock(JoinColumn.class);
    when(joinColumn.getReferencedField()).thenReturn("a");
    PostgresObjectField objectField = mock(PostgresObjectField.class);
    when(objectField.getJoinColumns()).thenReturn(List.of(joinColumn));

    PostgresObjectField objectFieldMock = mock(PostgresObjectField.class);
    when(objectFieldMock.getMappedByObjectField()).thenReturn(objectField);
    ObjectType objectType = mock(PostgresObjectType.class);
    PostgresObjectField objectFieldMock2 = mock(PostgresObjectField.class);
    when(objectFieldMock2.getColumn()).thenReturn("a");
    when(objectType.getField(anyString())).thenReturn(Optional.of(objectFieldMock2));
    when(objectFieldMock.getObjectType()).thenReturn(objectType);

    when(requestContext.getObjectField()).thenReturn(objectFieldMock);

    JoinCriteria joinCriteria = mock(JoinCriteria.class);
    Set<Map<String, Object>> keys = new HashSet<>();
    keys.add(Map.of("a", "b", "c", "d"));
    when(joinCriteria.getKeys()).thenReturn(keys);

    var result = selectBuilder.build(collectionRequestBuilder.build(), joinCriteria);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result.toString()
        .contains("from (values ('b')) as  (\"a\")\n" + "  left outer join lateral (\n" + "    select"));
  }

  private void initObjectRequestBuilder() {
    List<KeyCriteria> keyCriteria = List.of();
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    when(objectType.getTable()).thenReturn("anyTable");
    PostgresObjectField objectField = mock(PostgresObjectField.class);
    when(objectType.getField(anyString())).thenReturn(Optional.ofNullable(objectField));
    Map<FieldRequest, ObjectRequest> objectFields = Map.of();

    Map<String, Object> mapValues = Map.of("a", "b");
    ContextCriteria contextCriteria = mock(ContextCriteria.class);
    when(contextCriteria.getValues()).thenReturn(mapValues);
    when(contextCriteria.getName()).thenReturn("Brewery");

    FieldRequest fieldRequest = mock(FieldRequest.class);
    when(fieldRequest.getName()).thenReturn("a");

    objectRequestBuilder = ObjectRequest.builder()
        .objectType(objectType)
        .objectFields(objectFields)
        .scalarFields(List.of(fieldRequest))
        .keyCriteria(keyCriteria)
        .contextCriteria(contextCriteria);
  }

  private void initSortCriteriaList(SortDirection direction) {
    PostgresObjectField sortCriteriaField = mock(PostgresObjectField.class);
    lenient().when(sortCriteriaField.getColumn())
        .thenReturn("a");
    lenient().when(sortCriteriaField.getName())
        .thenReturn("a");

    SortCriteria.SortCriteriaBuilder sortCriteria = SortCriteria.builder()
        .fields(List.of(sortCriteriaField))
        .direction(direction);
    sortCriteriaList = List.of(sortCriteria.build());

    ScalarFieldMapper<Map<String, Object>> leafFieldMapper = mock(ScalarFieldMapper.class);
    lenient().when(leafFieldMapper.getAlias())
        .thenReturn("a");
    lenient().when(fieldMapper.getLeafFieldMapper(any(List.class)))
        .thenReturn(leafFieldMapper);
  }

  private void initCollectionRequest() {
    collectionRequestBuilder = CollectionRequest.builder()
        .objectRequest(objectRequestBuilder.build())
        .sortCriterias(sortCriteriaList);
  }
}
