package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.jooq.conf.ParamType.INLINED;
import static org.jooq.conf.ParamType.NAMED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.ObjectSelectContext;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilder;
import org.dotwebstack.framework.backend.postgres.query.SelectQueryBuilderResult;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderTest {

  @Mock
  private DatabaseClient databaseClient;

  @Mock
  private SelectQueryBuilder selectQueryBuilder;

  private PostgresDataLoader postgresDataLoader;

  @BeforeEach
  void beforeAll() {
    postgresDataLoader = new PostgresDataLoader(databaseClient, selectQueryBuilder);
  }

  @Test
  void supports_returnsTrue_withPostgresTypeConfiguration() {
    boolean supported = postgresDataLoader.supports(new PostgresTypeConfiguration());

    assertThat(supported, is(Boolean.TRUE));
  }

  @Test
  void supports_returnsFalse_withNonPostgresTypeConfiguration() {
    boolean supported = postgresDataLoader.supports(new AbstractTypeConfiguration<>() {
      @Override
      public void init(DotWebStackConfiguration dotWebStackConfiguration) {}

      @Override
      public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
        return null;
      }

      @Override
      public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
        return null;
      }

      @Override
      public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
        return null;
      }
    });

    assertThat(supported, is(Boolean.FALSE));
  }

  @Test
  void loadSingle() {
    var keyCondition = mock(KeyCondition.class);
    var loadEnvironment = mockLoadEnvironment();
    assertThrows(UnsupportedOperationException.class,
        () -> postgresDataLoader.loadSingle(keyCondition, loadEnvironment));
  }

  @Test
  void batchLoadSingle() {
    Set<KeyCondition> keyConditions = Set.of();
    var loadEnvironment = mockLoadEnvironment();
    assertThrows(UnsupportedOperationException.class,
        () -> postgresDataLoader.batchLoadSingle(keyConditions, loadEnvironment));
  }

  @Test
  void loadMany() {
    var keyCondition = mock(KeyCondition.class);
    var loadEnvironment = mockLoadEnvironment();
    assertThrows(UnsupportedOperationException.class, () -> postgresDataLoader.loadMany(keyCondition, loadEnvironment));
  }

  @Test
  void batchLoadMany() {
    Set<KeyCondition> keyConditions = Set.of();
    var loadEnvironment = mockLoadEnvironment();
    assertThrows(UnsupportedOperationException.class,
        () -> postgresDataLoader.batchLoadMany(keyConditions, loadEnvironment));
  }

  @Test
  void loadSingleRequest() {
    Map<String, Object> data = Map.of("x1", "id-1", "x2", "Brewery 1");
    var fetchSpec = mockQueryContext();
    when(fetchSpec.all()).thenReturn(Flux.just(data));

    SelectQuery<?> query = mock(SelectQuery.class);
    when(query.getSQL(INLINED)).thenReturn("");

    SelectQueryBuilderResult selectQueryBuilderResult = SelectQueryBuilderResult.builder()
        .query(query)
        .table(mock(Table.class))
        .mapAssembler(row -> row)
        .context(new ObjectSelectContext())
        .build();

    when(selectQueryBuilder.build(any(ObjectRequest.class), any(ObjectSelectContext.class)))
        .thenReturn(selectQueryBuilderResult);

    ObjectRequest objectRequest = ObjectRequest.builder()
        .typeConfiguration(new PostgresTypeConfiguration())
        .build();

    Map<String, Object> result = postgresDataLoader.loadSingleRequest(objectRequest)
        .block(Duration.ofSeconds(5));

    assertThat(result, notNullValue());
    assertThat(data.entrySet(), equalTo(result.entrySet()));

    verify(databaseClient, times(1)).sql(anyString());
    verify(fetchSpec, times(1)).all();
    verify(selectQueryBuilder, times(1)).build(any(ObjectRequest.class), any(ObjectSelectContext.class));
  }

  @Test
  void batchLoadSingleRequest() {
    ObjectRequest objectRequest = ObjectRequest.builder()
        .typeConfiguration(new PostgresTypeConfiguration())
        .build();

    assertThrows(UnsupportedOperationException.class, () -> postgresDataLoader.batchLoadSingleRequest(objectRequest));
  }

  @Test
  void loadManyRequest() {
    List<Map<String, Object>> data =
        List.of(Map.of("x1", "id-1", "x2", "Brewery 1"), Map.of("x1", "id-2", "x2", "Brewery 2"));
    var fetchSpec = mockQueryContext();
    when(fetchSpec.all()).thenReturn(Flux.fromIterable(data));

    SelectQuery<?> query = mock(SelectQuery.class);
    when(query.getSQL(INLINED)).thenReturn("");

    SelectQueryBuilderResult selectQueryBuilderResult = SelectQueryBuilderResult.builder()
        .query(query)
        .table(mock(Table.class))
        .mapAssembler(row -> row)
        .context(new ObjectSelectContext())
        .build();

    when(selectQueryBuilder.build(any(CollectionRequest.class), any(ObjectSelectContext.class)))
        .thenReturn(selectQueryBuilderResult);

    CollectionRequest objectRequest = CollectionRequest.builder()
        .objectRequest(ObjectRequest.builder()
            .typeConfiguration(new PostgresTypeConfiguration())
            .build())
        .build();

    List<Map<String, Object>> result = postgresDataLoader.loadManyRequest(null, objectRequest)
        .toStream()
        .collect(Collectors.toList());

    assertThat(result, notNullValue());
    assertThat(data, equalTo(result));

    verify(databaseClient, times(1)).sql(anyString());
    verify(fetchSpec, times(1)).all();
    verify(selectQueryBuilder, times(1)).build(any(CollectionRequest.class), any(ObjectSelectContext.class));
  }

  @Test
  void loadManyRequestWithKeyCondition() {
    List<Map<String, Object>> data =
        List.of(Map.of("x1", "id-1", "x2", "Beer 1"), Map.of("x1", "id-2", "x2", "Beer 2"));
    var fetchSpec = mockQueryContext();
    when(fetchSpec.all()).thenReturn(Flux.fromIterable(data));

    SelectQuery<?> query = mock(SelectQuery.class);
    when(query.getSQL(INLINED)).thenReturn("");

    KeyCondition keyCondition = ColumnKeyCondition.builder()
        .valueMap(Map.of("identifier", "id-1"))
        .joinTable(mock(JoinTable.class))
        .build();

    SelectQueryBuilderResult selectQueryBuilderResult = SelectQueryBuilderResult.builder()
        .query(query)
        .table(mock(Table.class))
        .mapAssembler(row -> row)
        .context(new ObjectSelectContext())
        .build();

    when(selectQueryBuilder.build(any(CollectionRequest.class), any(ObjectSelectContext.class)))
        .thenReturn(selectQueryBuilderResult);

    CollectionRequest collectionRequest = CollectionRequest.builder()
        .objectRequest(ObjectRequest.builder()
            .typeConfiguration(new PostgresTypeConfiguration())
            .build())
        .build();

    List<Map<String, Object>> result = postgresDataLoader.loadManyRequest(keyCondition, collectionRequest)
        .toStream()
        .collect(Collectors.toList());

    assertThat(result, notNullValue());
    assertThat(data, equalTo(result));

    verify(databaseClient, times(1)).sql(anyString());
    verify(fetchSpec, times(1)).all();
    verify(selectQueryBuilder, times(1)).build(any(CollectionRequest.class), any(ObjectSelectContext.class));
  }

  @Test
  void batchLoadManyRequest() {
    List<Map<String, Object>> data =
        List.of(Map.of("x1", "id-1", "x2", "Brewery 1"), Map.of("x1", "id-2", "x2", "Brewery 2"));
    var fetchSpec = mockQueryContext();
    when(fetchSpec.all()).thenReturn(Flux.fromIterable(data));

    SelectQuery<?> query = mock(SelectQuery.class);
    when(query.getSQL(NAMED)).thenReturn("");
    when(query.getParams()).thenReturn(Map.of());

    Set<KeyCondition> keyConditions = Set.of(ColumnKeyCondition.builder()
        .valueMap(Map.of("identifier", "id-1"))
        .build(),
        ColumnKeyCondition.builder()
            .valueMap(Map.of("identifier", "id-2"))
            .build());

    SelectQueryBuilderResult selectQueryBuilderResult = SelectQueryBuilderResult.builder()
        .query(query)
        .table(mock(Table.class))
        .mapAssembler(row -> row)
        .context(new ObjectSelectContext())
        .build();
    selectQueryBuilderResult.getContext()
        .setKeyColumnNames(Map.of("identifier", "x1"));

    when(selectQueryBuilder.build(any(CollectionRequest.class), any(ObjectSelectContext.class)))
        .thenReturn(selectQueryBuilderResult);
    CollectionRequest objectRequest = CollectionRequest.builder()
        .objectRequest(ObjectRequest.builder()
            .keyCriteria(new ArrayList<>())
            .typeConfiguration(new PostgresTypeConfiguration())
            .build())
        .build();

    List<Map<String, Object>> result = postgresDataLoader.batchLoadManyRequest(keyConditions, objectRequest)
        .flatMap(group -> group.map(d -> d))
        .toStream()
        .collect(Collectors.toList());

    assertThat(result, notNullValue());
    assertThat(data, equalTo(result));

    verify(databaseClient, times(1)).sql(anyString());
    verify(fetchSpec, times(1)).all();
    verify(selectQueryBuilder, times(1)).build(any(CollectionRequest.class), any(ObjectSelectContext.class));
  }

  @Test
  void useRequestApproach_returnsTrue_default() {
    assertThat(postgresDataLoader.useRequestApproach(), is(true));
  }

  @SuppressWarnings("unchecked")
  private FetchSpec<Map<String, Object>> mockQueryContext() {
    DatabaseClient.GenericExecuteSpec genericExecuteSpec = mock(DatabaseClient.GenericExecuteSpec.class);

    var fetchSpec = mock(FetchSpec.class);

    when(genericExecuteSpec.fetch()).thenReturn(fetchSpec);

    when(databaseClient.sql(any(String.class))).thenReturn(genericExecuteSpec);

    return fetchSpec;
  }

  private LoadEnvironment mockLoadEnvironment() {
    return LoadEnvironment.builder()
        .executionStepInfo(mock(ExecutionStepInfo.class))
        .selectionSet(mock(DataFetchingFieldSelectionSet.class))
        .build();
  }
}
