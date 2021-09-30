package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jObjectType;
import org.dotwebstack.framework.core.backend.BackendModule;

public class Rdf4jBackendModule implements BackendModule<Rdf4jObjectType> {

  @Override
  public Class<Rdf4jObjectType> getObjectTypeClass() {
    return Rdf4jObjectType.class;
  }
}
