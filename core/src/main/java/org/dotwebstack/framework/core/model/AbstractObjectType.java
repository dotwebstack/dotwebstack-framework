package org.dotwebstack.framework.core.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

@Data
public abstract class AbstractObjectType<T extends ObjectField> implements ObjectType<T> {

  protected String name;

  protected Map<String, T> fields;

  protected Map<String, List<SortableByConfiguration>> sortableBy;

  public Optional<T> getField(String name) {
    return Optional.ofNullable(fields.get(name));
  }
}
