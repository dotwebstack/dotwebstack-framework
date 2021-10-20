package org.dotwebstack.framework.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import lombok.Data;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

@Data
public abstract class AbstractObjectType<T extends ObjectField> implements ObjectType<T> {

  protected String name;

  @Valid
  protected Map<String, T> fields;

  @Valid
  protected Map<String, List<SortableByConfiguration>> sortableBy = new HashMap<>();

  @Valid
  protected Map<String, FilterConfiguration> filters = new HashMap<>();

  public Optional<T> getField(String name) {
    return Optional.ofNullable(fields.get(name));
  }
}
