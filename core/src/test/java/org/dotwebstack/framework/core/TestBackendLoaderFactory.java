package org.dotwebstack.framework.core;

import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.model.ObjectType;
import org.springframework.r2dbc.core.DatabaseClient;

public class TestBackendLoaderFactory implements BackendLoaderFactory {

  private final DatabaseClient databaseClient;

  public TestBackendLoaderFactory(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public <T extends ObjectType<?>> BackendLoader create(T objectType) {
    return new TestBackendLoader(databaseClient);
  }
}
