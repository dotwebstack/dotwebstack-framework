package org.dotwebstack.framework.backend.postgres;

import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.model.ObjectType;
import org.springframework.stereotype.Component;

@Component
public class PostgresBackendLoaderFactory implements BackendLoaderFactory {

  private final PostgresClient postgresClient;

  public PostgresBackendLoaderFactory(PostgresClient postgresClient) {
    this.postgresClient = postgresClient;
  }

  @Override
  public <T extends ObjectType<?>> BackendLoader create(T objectType) {
    return new PostgresBackendLoader(postgresClient);
  }
}
