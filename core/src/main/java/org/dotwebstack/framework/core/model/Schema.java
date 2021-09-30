package org.dotwebstack.framework.core.model;

import java.util.Map;
import java.util.Optional;
import lombok.Data;

@Data
public class Schema {

  private Map<String, ObjectType<? extends ObjectField>> objectTypes;

  public Optional<ObjectType<? extends ObjectField>> getObjectType(String name) {
    return Optional.ofNullable(objectTypes.get(name));
  }
}
