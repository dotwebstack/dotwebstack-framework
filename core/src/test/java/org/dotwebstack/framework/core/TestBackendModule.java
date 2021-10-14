package org.dotwebstack.framework.core;

import java.util.Map;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.model.ObjectType;

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

  @Override
  public void init(Map<String, ObjectType<?>> objectTypes) {

  }
}
