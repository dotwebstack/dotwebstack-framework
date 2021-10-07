package org.dotwebstack.framework.core.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class Schema {

  public static final String PAGING = "paging";

  @Getter(AccessLevel.NONE)
  private List<String> features = List.of();

  private Map<String, ObjectType<? extends ObjectField>> objectTypes;

  public Optional<ObjectType<? extends ObjectField>> getObjectType(String name) {
    return Optional.ofNullable(objectTypes.get(name));
  }

  public boolean usePaging() {
    return features.contains(PAGING);
  }
}
