package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.QueryBuilder;
import org.dotwebstack.framework.backend.postgres.query.QueryHolder;
import org.dotwebstack.framework.backend.postgres.query.QueryParameters;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderTest {

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  private DatabaseClient databaseClient;

  @Mock
  private QueryBuilder queryBuilder;

  private FetchSpec<Map<String, Object>> fetchSpec;

  private PostgresDataLoader postgresDataLoader;

  @BeforeEach
  void beforeAll() {
    postgresDataLoader = new PostgresDataLoader(dotWebStackConfiguration, databaseClient, queryBuilder);
  }

  @Test
  void supports_returnsTrue_withPostgresTypeConfiguration() {
    // Arrange & Act
    boolean supported = postgresDataLoader.supports(new PostgresTypeConfiguration());

    // Assert
    assertThat(supported, is(Boolean.TRUE));
  }

  @Test
  void supports_returnsFalse_withNonPostgresTypeConfiguration() {
    // Arrange & Act
    boolean supported = postgresDataLoader.supports(new AbstractTypeConfiguration<>() {
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

    // Assert
    assertThat(supported, is(Boolean.FALSE));
  }

  @Test
  void loadSingle() {
    // Arrange
    mockQueryContext();

    Map<String, Object> data = Map.of("x1", "id-1", "x2", "Brewery 1");

    ColumnKeyCondition keyCondition = mock(ColumnKeyCondition.class);

    when(fetchSpec.one()).thenReturn(Mono.just(data));

    Query query = mock(Query.class);
    when(query.getSQL(ParamType.NAMED)).thenReturn("");
    when(query.getParams()).thenReturn(Map.of());

    QueryHolder queryHolder = QueryHolder.builder()
        .query(query)
        .mapAssembler(row -> row)
        .build();

    when(queryBuilder.build(any(PostgresTypeConfiguration.class), any(QueryParameters.class))).thenReturn(queryHolder);

    LoadEnvironment loadEnvironment = mockLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment))
        .thenReturn(mock(PostgresTypeConfiguration.class));

    // Act
    Map<String, Object> result = postgresDataLoader.loadSingle(keyCondition, loadEnvironment)
        .block(Duration.ofSeconds(5));

    // Assert
    assertThat(result, notNullValue());
    assertThat(data.entrySet(), equalTo(result.entrySet()));

    verify(databaseClient, times(1)).sql(anyString());
    verify(fetchSpec, times(1)).one();
    verify(queryBuilder, times(1)).build(any(PostgresTypeConfiguration.class), any(QueryParameters.class));
  }

  @Test
  void batchLoadSingle() {
    // Act & Assert
    assertThrows(UnsupportedOperationException.class,
        () -> postgresDataLoader.batchLoadSingle(Set.of(), mockLoadEnvironment()));
  }

  @Test
  void loadMany() {
    // Arrange
    mockQueryContext();

    List<Map<String, Object>> data =
        List.of(Map.of("x1", "id-1", "x2", "Brewery 1"), Map.of("x1", "id-2", "x2", "Brewery 2"));

    ColumnKeyCondition keyCondition = mock(ColumnKeyCondition.class);

    when(fetchSpec.all()).thenReturn(Flux.fromIterable(data));

    Query query = mock(Query.class);
    when(query.getSQL(ParamType.NAMED)).thenReturn("");
    when(query.getParams()).thenReturn(Map.of());

    QueryHolder queryHolder = QueryHolder.builder()
        .query(query)
        .mapAssembler(row -> row)
        .build();

    when(queryBuilder.build(any(PostgresTypeConfiguration.class), any(QueryParameters.class))).thenReturn(queryHolder);

    LoadEnvironment loadEnvironment = mockLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment))
        .thenReturn(mock(PostgresTypeConfiguration.class));

    // Act
    List<Map<String, Object>> result = postgresDataLoader.loadMany(keyCondition, loadEnvironment)
        .toStream()
        .collect(Collectors.toList());

    // Assert
    assertThat(result, notNullValue());
    assertThat(data, equalTo(result));

    verify(databaseClient, times(1)).sql(anyString());
    verify(fetchSpec, times(1)).all();
    verify(queryBuilder, times(1)).build(any(PostgresTypeConfiguration.class), any(QueryParameters.class));
  }

  @Test
  void batchLoadMany() {
    // Arrange
    mockQueryContext();

    List<Map<String, Object>> data =
        List.of(Map.of("x1", "id-1", "x2", "Brewery 1"), Map.of("x1", "id-2", "x2", "Brewery 2"));

    Set<KeyCondition> keyConditions = Set.of(ColumnKeyCondition.builder()
        .valueMap(Map.of("identifier", "id-1"))
        .build(),
        ColumnKeyCondition.builder()
            .valueMap(Map.of("identifier", "id-2"))
            .build());

    when(fetchSpec.all()).thenReturn(Flux.fromIterable(data));

    Query query = mock(Query.class);
    when(query.getSQL(ParamType.NAMED)).thenReturn("");
    when(query.getParams()).thenReturn(Map.of());

    QueryHolder queryHolder = QueryHolder.builder()
        .query(query)
        .mapAssembler(row -> row)
        .keyColumnNames(Map.of("identifier", "x1"))
        .build();

    when(queryBuilder.build(any(PostgresTypeConfiguration.class), any(QueryParameters.class))).thenReturn(queryHolder);

    LoadEnvironment loadEnvironment = mockLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment))
        .thenReturn(mock(PostgresTypeConfiguration.class));

    // Act
    List<Map<String, Object>> result = postgresDataLoader.batchLoadMany(keyConditions, loadEnvironment)
        .flatMap(group -> group.map(d -> d))
        .toStream()
        .collect(Collectors.toList());

    // Assert
    assertThat(result, notNullValue());
    assertThat(data, equalTo(result));

    verify(databaseClient, times(1)).sql(anyString());
    verify(fetchSpec, times(1)).all();
    verify(queryBuilder, times(1)).build(any(PostgresTypeConfiguration.class), any(QueryParameters.class));
  }

  @SuppressWarnings("unchecked")
  private void mockQueryContext() {
    DatabaseClient.GenericExecuteSpec genericExecuteSpec = mock(DatabaseClient.GenericExecuteSpec.class);

    fetchSpec = mock(FetchSpec.class);

    when(genericExecuteSpec.fetch()).thenReturn(fetchSpec);

    when(databaseClient.sql(any(String.class))).thenReturn(genericExecuteSpec);
  }

  private LoadEnvironment mockLoadEnvironment() {
    return LoadEnvironment.builder()
        .executionStepInfo(mock(ExecutionStepInfo.class))
        .selectionSet(mock(DataFetchingFieldSelectionSet.class))
        .build();
  }
}
