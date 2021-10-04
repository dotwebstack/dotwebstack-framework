package org.dotwebstack.framework.core.backend.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.model.ObjectField;

@Getter
public class RowMapper<T> extends AbstractObjectMapper<T> {

  @SuppressWarnings("unchecked")
  public ScalarFieldMapper<T> getLeafFieldMapper(List<ObjectField> fieldPath) {
    var fields = new ArrayList<>(fieldPath);
    var finalField = fields.remove(fields.size() - 1);

    ObjectFieldMapper<T> finalObjectFieldMapper = this;

    for (var field : fields) {
      var nestedFieldMapper = finalObjectFieldMapper.getFieldMapper(field.getName());

      if (nestedFieldMapper instanceof ObjectFieldMapper) {
        finalObjectFieldMapper = (ObjectFieldMapper<T>) nestedFieldMapper;
      } else {
        throw ExceptionHelper.illegalStateException("Non-final path segments must have an object field mapper.");
      }
    }

    var fieldMapper = finalObjectFieldMapper.getFieldMapper(finalField.getName());

    if (fieldMapper instanceof ScalarFieldMapper) {
      return (ScalarFieldMapper<T>) fieldMapper;
    }

    throw illegalArgumentException("Scalar field mapper {} not found.", finalField.getName());
  }
}
