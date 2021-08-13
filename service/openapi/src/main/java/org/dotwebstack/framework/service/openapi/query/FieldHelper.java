package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;

public class FieldHelper {

  private FieldHelper() {}

  public static Field resolveField(@NonNull GraphQlQuery query, @NonNull String[] path) {
    Field field = query.getField();
    if (path.length <= 1) {
      return field;
    }
    List<Field> search = skipNodeFields(field.getChildren());
    return resolveField(search, path);
  }

  protected static Field resolveField(@NonNull List<Field> fields, @NonNull String[] path) {
    Field result = null;
    for (int i = 0; i < path.length - 1; i++) {
      int finalI = i;
      Field finalResult = result;
      result = fields.stream()
          .filter(f -> f.getName()
              .equals(path[finalI]))
          .findFirst()
          .orElseThrow(() -> invalidConfigurationException("Could not resolve path {} for field {}", path,
              finalResult != null ? finalResult.getName() : "root"));
      fields = skipNodeFields(result.getChildren());
    }
    return result;
  }

  protected static List<Field> skipNodeFields(List<Field> fields) {
    if (fields.size() == 1 && fields.get(0)
        .isNodeField()) {
      return skipNodeFields(fields.get(0)
          .getChildren());
    }
    return fields;
  }

}
