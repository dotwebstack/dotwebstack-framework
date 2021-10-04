package org.dotwebstack.framework.core.backend.query;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public abstract class AbstractObjectMapper<T> implements ObjectFieldMapper<T> {

  protected final Map<String, FieldMapper<T, ?>> fieldMappers = new HashMap<>();

  @Override
  public Map<String, Object> apply(T row) {
    return fieldMappers.entrySet()
        .stream()
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()
            .apply(row)), HashMap::putAll);
  }

  public void register(String name, FieldMapper<T, ?> fieldMapper) {
    fieldMappers.put(name, fieldMapper);
  }

  public FieldMapper<T, ?> getFieldMapper(String name) {
    return fieldMappers.get(name);
  }
}
