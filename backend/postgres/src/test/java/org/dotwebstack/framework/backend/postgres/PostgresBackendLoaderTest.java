package org.dotwebstack.framework.backend.postgres;


import org.junit.jupiter.api.BeforeEach;
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
