package org.dotwebstack.framework.ext.spatial.testhelper;

import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;

public class TestSpatialBackendModule implements SpatialBackendModule<TestSpatialReferenceSystem> {

  @Override
  public Class<TestSpatialReferenceSystem> getSpatialReferenceSystemClass() {
    return TestSpatialReferenceSystem.class;
  }
}
