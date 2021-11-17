package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jSpatialReferenceSystem;
import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.springframework.stereotype.Component;

@Component
class Rdf4jSpatialBackendModule implements SpatialBackendModule<Rdf4jSpatialReferenceSystem> {

  @Override
  public Class<Rdf4jSpatialReferenceSystem> getSpatialReferenceSystemClass() {
    return Rdf4jSpatialReferenceSystem.class;
  }

  @Override
  public void init(Spatial spatial) {
    // Not implemented
  }
}
