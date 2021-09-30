package org.dotwebstack.framework.core.backend;

import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

public interface BackendModule<T extends ObjectType<? extends ObjectField>> {

  Class<T> getObjectTypeClass();
}
