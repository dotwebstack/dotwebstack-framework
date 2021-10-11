package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
public class PostgresBackendLoaderTest {
  
  @Mock
  private DatabaseClient databaseClient;
  
  private PostgresBackendLoader backendLoader;
  
  @BeforeEach
  void doBeforeEach() {
    backendLoader = new PostgresBackendLoader(databaseClient);
  }
  
}
