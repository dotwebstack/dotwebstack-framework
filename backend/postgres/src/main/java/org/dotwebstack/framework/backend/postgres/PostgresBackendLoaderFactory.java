package org.dotwebstack.framework.backend.postgres;

import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.model.ObjectType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class PostgresBackendLoaderFactory implements BackendLoaderFactory {

  private final DatabaseClient databaseClient;

  public PostgresBackendLoaderFactory(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public <T extends ObjectType<?>> BackendLoader create(T objectType) {
    return new PostgresBackendLoader(databaseClient);
  }
}
