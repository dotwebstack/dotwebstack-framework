package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
class PostgresBackendModuleTest {

  @Mock
  private DatabaseClient databaseClient;

  private PostgresBackendLoaderFactory backendLoaderFactory;

  @BeforeEach
  void doBeforeEach() {
    backendLoaderFactory = new PostgresBackendLoaderFactory(databaseClient);
  }

  @Test
  void getBackendLoaderFactory_shouldReturn_BackendLoaderFactory() {
    PostgresObjectType objectType = new PostgresObjectType();
    var result = backendLoaderFactory.create(objectType);

    assertThat(result, is(notNullValue()));
    assertTrue(result instanceof PostgresBackendLoader);
  }
}
