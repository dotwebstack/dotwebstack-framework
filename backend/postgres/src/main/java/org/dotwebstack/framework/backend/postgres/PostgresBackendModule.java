package org.dotwebstack.framework.backend.postgres;

import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.springframework.stereotype.Component;

@Component
class PostgresBackendModule implements BackendModule<PostgresObjectType> {

  private final PostgresBackendLoaderFactory backendLoaderFactory;

  public PostgresBackendModule(PostgresBackendLoaderFactory backendLoaderFactory) {
    this.backendLoaderFactory = backendLoaderFactory;
  }

  @Override
  public Class<PostgresObjectType> getObjectTypeClass() {
    return PostgresObjectType.class;
  }

  @Override
  public BackendLoaderFactory getBackendLoaderFactory() {
    return backendLoaderFactory;
  }
}
