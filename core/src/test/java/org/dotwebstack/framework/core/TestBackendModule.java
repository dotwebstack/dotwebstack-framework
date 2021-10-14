package org.dotwebstack.framework.core;

import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;

public class TestBackendModule implements BackendModule<TestObjectType> {
  private final TestBackendLoaderFactory backendLoaderFactory;

  public TestBackendModule(TestBackendLoaderFactory backendLoaderFactory) {
    this.backendLoaderFactory = backendLoaderFactory;
  }

  @Override
  public Class<TestObjectType> getObjectTypeClass() {
    return TestObjectType.class;
  }

  @Override
  public BackendLoaderFactory getBackendLoaderFactory() {
    return backendLoaderFactory;
  }
}
