package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jSpatialReferenceSystem;
import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.springframework.stereotype.Component;

@Component
class Rdf4jSpatialBackendModule implements SpatialBackendModule<Rdf4jSpatialReferenceSystem> {

  @Override
  public Class<Rdf4jSpatialReferenceSystem> getSpatialReferenceSystemClass() {
    return Rdf4jSpatialReferenceSystem.class;
  }
}
