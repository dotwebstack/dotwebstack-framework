package org.dotwebstack.framework.ext.spatial.backend;

import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;

public interface SpatialBackendModule<T extends SpatialReferenceSystem> {

  Class<T> getSpatialReferenceSystemClass();

}
