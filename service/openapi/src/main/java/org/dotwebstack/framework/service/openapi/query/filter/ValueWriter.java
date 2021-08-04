package org.dotwebstack.framework.service.openapi.query.filter;

import java.util.List;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public class ValueWriter {
  private ValueWriter() {}

  public static void write(Object value, StringBuilder sb) {
    if (value instanceof String) {
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
      sb.append(value.toString());
    } else {
      throw ExceptionHelper.illegalArgumentException("GraphpQl query builder does not support serialization of type {}",
          value.getClass()
              .getName());
    }
  }

}
