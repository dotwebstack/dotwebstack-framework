package org.dotwebstack.framework.service.openapi.param;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.Schema;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ParamValueCaster {


  public static Object cast(Object value, Schema<?> schema) {
    if (value == null) {
      return null;
    }
    if ("string".equals(schema.getType())) {
      return value;
    }
    try {
      if ("number".equals(schema.getType()) && "float".equals(schema.getType())) {
        return Float.parseFloat((String) value);
      } else if ("number".equals(schema.getType()) && "double".equals(schema.getType())) {
        return Double.parseDouble((String) value);
      } else if ("number".equals(schema.getType()) && "int64".equals(schema.getType())) {
        return Long.parseLong((String) value);
      } else if ("integer".equals(schema.getType())
          || ("number".equals(schema.getType()) && "int32".equals(schema.getType()))) {
        return Integer.parseInt((String) value);
      } else if ("number".equals(schema.getType())) {
        return new BigDecimal((String) value);
      } else if ("boolean".equals(schema.getType())) {
        return Boolean.parseBoolean((String) value);
      } else {
        throw illegalArgumentException("Could not cast scalar value [{}] with schema type [{}] " + "and format {}",
            value, schema.getType(), schema.getFormat());
      }
    } catch (NumberFormatException e) {
      throw parameterValidationException("Could not cast scalar value [{}] with schema type [{}] " + "and format {}",
          value, schema.getType(), schema.getFormat(), e);
    }

  }

  public static ImmutableList<?> castList(List<?> list, Schema<?> schema) {
    return list.stream()
        .map(i -> cast(i, schema))
        .collect(collectingAndThen(toList(), ImmutableList::copyOf));
  }

  public static ImmutableList<?> castArray(Object[] array, Schema<?> schema) {
    return castList(Arrays.asList(array), schema);
  }
}
