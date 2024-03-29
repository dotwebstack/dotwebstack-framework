package org.dotwebstack.framework.core.model;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

@Data
@EqualsAndHashCode(exclude = {"fields", "implementz"})
public abstract class AbstractObjectType<T extends ObjectField> implements ObjectType<T> {

  protected String name;

  @JsonProperty("implements")
  @Getter(AccessLevel.NONE)
  protected List<String> implementz;

  @Valid
  protected Map<String, T> fields = new HashMap<>();

  @Valid
  protected Map<String, List<SortableByConfiguration>> sortableBy = new HashMap<>();

  @Valid
  protected Map<String, FilterConfiguration> filters = new HashMap<>();

  public T getField(String name) {
    return ofNullable(fields.get(name))
        .orElseThrow(() -> illegalArgumentException("Field '{}' does not exist in object type '{}'.", name, getName()));
  }

  public void addField(String name, ObjectField field) {
    fields.putIfAbsent(name, (T) field);
  }

  @Override
  public List<String> getImplements() {
    if (implementz == null) {
      return Collections.emptyList();
    }
    return implementz;
  }

  @Override
  public boolean isNested() {
    return false;
  }

  protected AbstractObjectType() {
    super();
  }
}
