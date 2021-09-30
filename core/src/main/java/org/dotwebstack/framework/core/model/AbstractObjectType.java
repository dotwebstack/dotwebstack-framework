package org.dotwebstack.framework.core.model;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.Data;

@Data
public abstract class AbstractObjectType<T extends ObjectField> implements ObjectType<T> {

  private String name;

  private Map<String, T> fields;

  public Collection<T> getFields() {
    return fields.values();
  }

  public Optional<T> getField(String name) {
    return Optional.ofNullable(fields.get(name));
  }
}
