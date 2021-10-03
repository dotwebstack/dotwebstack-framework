package org.dotwebstack.framework.core.backend.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.model.ObjectField;

@Getter
public abstract class AbstractRowMapper<T> implements FieldMapper<T> {

  protected final Map<String, FieldMapper<T>> fieldMappers = new HashMap<>();

  @Override
  public Object apply(T row) {
    return fieldMappers.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()
            .apply(row)));
  }

  public void register(String name, FieldMapper<T> fieldMapper) {
    fieldMappers.put(name, fieldMapper);
  }

  public FieldMapper<T> getFieldMapper(String name) {
    return fieldMappers.get(name);
  }

  @SuppressWarnings("unchecked")
  public ScalarFieldMapper<T> getScalarFieldMapper(List<ObjectField> fieldPath) {
    var fields = new ArrayList<>(fieldPath);
    var finalField = fields.remove(fields.size() - 1);

    AbstractRowMapper<T> nestedMapper = this;

    for (var field : fields) {
      var nestedFieldMapper = nestedMapper.getFieldMapper(field.getName());

      if (nestedFieldMapper instanceof AbstractRowMapper) {
        nestedMapper = (AbstractRowMapper<T>) nestedFieldMapper;
      } else {
        throw ExceptionHelper.illegalStateException("Field path is not correct.");
      }
    }

    var fieldMapper = nestedMapper.getFieldMapper(finalField.getName());

    if (fieldMapper instanceof ScalarFieldMapper) {
      return (ScalarFieldMapper<T>) fieldMapper;
    }

    throw illegalArgumentException("Scalar field mapper {} not found.", finalField.getName());
  }
}
