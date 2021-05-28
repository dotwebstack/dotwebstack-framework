package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.backend.postgres.query.objectquery.ObjectQueryBuilder;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderTest {

  @Mock
  private DatabaseClient databaseClient;

  @Mock
  private ObjectQueryBuilder objectQueryBuilder;

  private PostgresDataLoader postgresDataLoader;

  @BeforeEach
  void beforeAll() {
    postgresDataLoader = new PostgresDataLoader(databaseClient, objectQueryBuilder);
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

  @SuppressWarnings("unchecked")
  private void mockQueryContext() {
    DatabaseClient.GenericExecuteSpec genericExecuteSpec = mock(DatabaseClient.GenericExecuteSpec.class);

    var fetchSpec = mock(FetchSpec.class);

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
