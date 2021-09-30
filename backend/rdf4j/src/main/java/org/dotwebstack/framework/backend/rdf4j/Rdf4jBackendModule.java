package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jObjectType;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.springframework.stereotype.Component;

@Component
class Rdf4jBackendModule implements BackendModule<Rdf4jObjectType> {

  private final Rdf4jBackendLoaderFactory backendLoaderFactory;

  public Rdf4jBackendModule(Rdf4jBackendLoaderFactory backendLoaderFactory) {
    this.backendLoaderFactory = backendLoaderFactory;
  }

  @Override
  public Class<Rdf4jObjectType> getObjectTypeClass() {
    return Rdf4jObjectType.class;
  }

  @Override
  public BackendLoaderFactory getBackendLoaderFactory() {
    return backendLoaderFactory;
  }
}
