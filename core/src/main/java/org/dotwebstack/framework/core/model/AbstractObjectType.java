package org.dotwebstack.framework.core.model;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

@Data
@EqualsAndHashCode(exclude = {"fields"})
public abstract class AbstractObjectType<T extends ObjectField> implements ObjectType<T> {

  protected String name;

  @Valid
  protected Map<String, T> fields = new HashMap<>();

  @Valid
  protected Map<String, List<SortableByConfiguration>> sortableBy = new HashMap<>();

  @Valid
  protected Map<String, FilterConfiguration> filters = new HashMap<>();

  public T getField(String name) {
    return ofNullable(fields.get(name)).orElseThrow(() -> new IllegalArgumentException(
        String.format("Field '%s' does not exist in object type '%s'.", name, getName())));
  }

  @Override
  public boolean isNested() {
    return false;
  }
}
