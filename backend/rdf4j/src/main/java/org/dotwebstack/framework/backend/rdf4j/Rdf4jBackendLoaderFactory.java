package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jObjectType;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.model.ObjectType;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jBackendLoaderFactory implements BackendLoaderFactory {

  @Override
  public <T extends ObjectType<?>> BackendLoader create(T objectType) {
    return new Rdf4jBackendLoader((Rdf4jObjectType) objectType);
  }
}
