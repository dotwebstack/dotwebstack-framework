package org.dotwebstack.framework.ext.spatial.testhelper;

import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.dotwebstack.framework.ext.spatial.model.Spatial;

public class TestSpatialBackendModule implements SpatialBackendModule<TestSpatialReferenceSystem> {

  @Override
  public Class<TestSpatialReferenceSystem> getSpatialReferenceSystemClass() {
    return TestSpatialReferenceSystem.class;
  }

  @Override
  public void init(Spatial spatial) {
    // Not implemented
  }
}
