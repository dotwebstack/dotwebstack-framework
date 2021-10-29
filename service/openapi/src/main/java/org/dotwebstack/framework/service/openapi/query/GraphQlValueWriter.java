package org.dotwebstack.framework.service.openapi.query;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public class GraphQlValueWriter {
  private GraphQlValueWriter() {}

  public static void write(Object value, StringBuilder sb) {
    if (value instanceof Map) {
      sb.append("{");
      String prefix = "";
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        sb.append(prefix);
        sb.append(entry.getKey()
            .toString())
            .append(": ");
        write(entry.getValue(), sb);
        prefix = ", ";
      }
      sb.append("}");
    } else if (value instanceof String) {
      sb.append("\"");
      sb.append((String) value);
      sb.append("\"");
    } else if (value instanceof List) {
      List<?> l = ((List<?>) value);
      sb.append("[");
      String prefix = "";
      for (Object o : l) {
        sb.append(prefix);
        write(o, sb);
        prefix = ", ";
      }
      sb.append("]");
    } else if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float
        || value instanceof Short) {
      sb.append(value);
    } else {
      throw ExceptionHelper.illegalArgumentException("GraphpQl query builder does not support serialization of type {}",
          value.getClass()
              .getName());
    }
  }

}
