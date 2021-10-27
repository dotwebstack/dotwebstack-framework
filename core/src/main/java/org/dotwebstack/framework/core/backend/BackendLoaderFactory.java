package org.dotwebstack.framework.core.backend;

import org.dotwebstack.framework.core.model.ObjectType;

public interface BackendLoaderFactory {

  <T extends ObjectType<?>> BackendLoader create(T objectType);
}
