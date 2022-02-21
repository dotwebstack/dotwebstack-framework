package org.dotwebstack.framework.core.backend.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

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
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()
            .apply(row)), HashMap::putAll);
  }

  public void register(String name, FieldMapper<T, ?> fieldMapper) {
    fieldMappers.put(name, fieldMapper);
  }

  public FieldMapper<T, ?> getFieldMapper(String name) {
    return fieldMappers.get(name);
  }

  @SuppressWarnings("unchecked")
  public ScalarFieldMapper<T> getLeafFieldMapper(List<ObjectField> fieldPath) {
    var fields = new ArrayList<>(fieldPath);
    var finalField = fields.remove(fields.size() - 1);

    ObjectFieldMapper<T> finalObjectFieldMapper = this;

    for (var field : fields) {
      var nestedFieldMapper = finalObjectFieldMapper.getFieldMapper(field.getName());

      if (nestedFieldMapper == null) {
        nestedFieldMapper = finalObjectFieldMapper.getFieldMapper(String.format("%s.%s", field.getName(), "$system"));
      }

      if (nestedFieldMapper instanceof ObjectFieldMapper) {
        finalObjectFieldMapper = (ObjectFieldMapper<T>) nestedFieldMapper;
      } else {
        throw illegalStateException("Non-final path segments must have an object field mapper.");
      }
    }

    var fieldMapper = finalObjectFieldMapper.getFieldMapper(finalField.getName());

    if (fieldMapper instanceof ScalarFieldMapper) {
      return (ScalarFieldMapper<T>) fieldMapper;
    }

    throw illegalArgumentException("Scalar field mapper {} not found.", finalField.getName());
  }
}
