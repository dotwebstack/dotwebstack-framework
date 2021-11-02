package org.dotwebstack.framework.backend.postgres;

import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.springframework.stereotype.Component;

@Component
class PostgresSpatialBackendModule implements SpatialBackendModule<PostgresSpatialReferenceSystem> {

  @Override
  public Class<PostgresSpatialReferenceSystem> getSpatialReferenceSystemClass() {
    return PostgresSpatialReferenceSystem.class;
  }
}
