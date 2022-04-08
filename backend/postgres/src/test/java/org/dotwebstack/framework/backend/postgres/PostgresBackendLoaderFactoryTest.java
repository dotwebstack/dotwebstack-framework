package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresBackendLoaderFactoryTest {

  @Mock
  private PostgresClient postgresClient;

  private PostgresBackendLoaderFactory backendLoaderFactory;

  private PostgresBackendModule backendModule;

  @BeforeEach
  void doBeforeEach() {
    backendLoaderFactory = new PostgresBackendLoaderFactory(postgresClient);
  }

  @Test
  void create_returnsBackendLoader() {
    var result = backendLoaderFactory.create(mock(PostgresObjectType.class));

    assertThat(result, CoreMatchers.is(Matchers.notNullValue()));
    assertTrue(result instanceof PostgresBackendLoader);
  }
}
