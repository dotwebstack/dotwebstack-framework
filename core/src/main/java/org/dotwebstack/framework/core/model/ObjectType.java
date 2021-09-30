package org.dotwebstack.framework.core.model;

import java.util.Collection;
import java.util.Optional;

public interface ObjectType<T extends ObjectField> {

  String getName();

  void setName(String name);

  Collection<T> getFields();

  Optional<T> getField(String name);
}
