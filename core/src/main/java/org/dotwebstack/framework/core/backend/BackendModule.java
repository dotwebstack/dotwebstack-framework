package org.dotwebstack.framework.core.backend;

import org.dotwebstack.framework.core.model.ObjectType;

public interface BackendModule<T extends ObjectType<?>> {

  Class<T> getObjectTypeClass();

  BackendLoaderFactory getBackendLoaderFactory();
}
