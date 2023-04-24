package org.dotwebstack.framework.core.backend.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ObjectFieldHelper.createSystemAlias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectField;

@Getter
public abstract class AbstractObjectMapper<T> implements ObjectFieldMapper<T> {

  protected final Map<String, FieldMapper<T, ?>> fieldMappers = new HashMap<>();

  @Override
  public Map<String, Object> apply(T row) {
    return fieldMappers.entrySet()
        .stream()
        .collect(HashMap::new, (map, entry) -> {
          if (entry.getKey().equals("json")) {
            var val = (Map<String, Object>) entry.getValue().apply(row);
            map.putAll(val);
          } else {
            map.put(entry.getKey(), entry.getValue().apply(row));
          }
        }, HashMap::putAll);
  }

  public void register(String name, FieldMapper<T, ?> fieldMapper) {
    fieldMappers.put(name, fieldMapper);
  }

  public FieldMapper<T, ?> getFieldMapper(String name) {
    return fieldMappers.get(name);
  }

  @SuppressWarnings("unchecked")
  public ScalarFieldMapper<T> getLeafFieldMapper(List<ObjectField> fieldPath) {
    var objectFields = new ArrayList<>(fieldPath);
    var finalField = objectFields.remove(objectFields.size() - 1);

    ObjectFieldMapper<T> current = this;
    for (var objectField : objectFields) {
      var nestedFieldMapper = getNestedFieldMapper(current, objectField);

      if (nestedFieldMapper instanceof ObjectFieldMapper) {
        current = (ObjectFieldMapper<T>) nestedFieldMapper;
      } else {
        throw illegalStateException("Non-final path segments must have an object field mapper.");
      }
    }

    var fieldMapper = current.getFieldMapper(finalField.getName());

    if (fieldMapper instanceof ScalarFieldMapper) {
      return (ScalarFieldMapper<T>) fieldMapper;
    }

    throw illegalArgumentException("Scalar field mapper {} not found.", finalField.getName());
  }

  private FieldMapper<T, ?> getNestedFieldMapper(ObjectFieldMapper<T> current, ObjectField objectField) {
    var nestedFieldMapper = current.getFieldMapper(objectField.getName());

    if (nestedFieldMapper == null) {
      nestedFieldMapper = current.getFieldMapper(createSystemAlias(objectField));
    }
    return nestedFieldMapper;
  }
}
