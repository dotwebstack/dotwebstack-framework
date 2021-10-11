package org.dotwebstack.framework.backend.postgres;

import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class PostgresBackendLoaderFactoryTest {
  
  @Mock
  private DatabaseClient databaseClient;
  
  private PostgresBackendLoaderFactory backendLoaderFactory;
  
  private PostgresBackendModule backendModule;
  
  @BeforeEach
  void doBeforeEach() {
    backendLoaderFactory = new PostgresBackendLoaderFactory(databaseClient);
    backendModule = new PostgresBackendModule(backendLoaderFactory);
  }
  
  @Test
  void getObjectTypeClass_shouldReturn_PostgresObjectTypeClass() {
    
    var result = backendModule.getObjectTypeClass();
    
    assertThat(result, is(notNullValue()));
    assertThat(result, is(PostgresObjectType.class));
  }
  
  @Test
  void getBackendLoaderFactory_shouldReturn_BackendLoaderFactory() {
    
    var result = backendModule.getBackendLoaderFactory();
    
    assertThat(result, is(notNullValue()));
    assertTrue(result instanceof BackendLoaderFactory);
    assertThat(result, is(backendLoaderFactory));
  }
}
