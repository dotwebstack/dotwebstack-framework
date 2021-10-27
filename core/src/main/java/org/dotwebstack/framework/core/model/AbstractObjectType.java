package org.dotwebstack.framework.core.model;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public T getField(String name) {
    return ofNullable(fields.get(name))
        .orElseThrow(() -> illegalArgumentException("Field '{}' doesn't exist in objectType '{}'", name, getName()));
  }
}
