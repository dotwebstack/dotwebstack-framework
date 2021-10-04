package org.dotwebstack.framework.core.backend.query;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public abstract class AbstractObjectMapper<T> implements ObjectFieldMapper<T> {

  protected final Map<String, FieldMapper<T, ?>> fieldMappers = new HashMap<>();

  @Override
  public Map<String, Object> apply(T row) {
    return fieldMappers.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()
            .apply(row)));
  }

  public void register(String name, FieldMapper<T, ?> fieldMapper) {
    fieldMappers.put(name, fieldMapper);
  }

  public FieldMapper<T, ?> getFieldMapper(String name) {
    return fieldMappers.get(name);
  }
}
