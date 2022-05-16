package org.dotwebstack.framework.service.openapi.param;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.Schema;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;

public class ParamValueCaster {

  public static final String NUMBER_TYPE = "number";

  public static final String INTEGER_TYPE = "integer";

  private ParamValueCaster() {}

  public static Object cast(String value, @NonNull Schema<?> schema) {
    if (value == null) {
      return null;
    }
    if ("string".equals(schema.getType())) {
      return value;
    }
    try {
      switch (schema.getType()) {
        case NUMBER_TYPE:
          return castNumber(value, schema.getFormat());
        case INTEGER_TYPE:
          return castInteger(value, schema.getFormat());
        case "boolean":
          return Boolean.parseBoolean(value);
        default:
          throw illegalArgumentException("Could not cast scalar value [{}] with schema type [{}] and format [{}]",
              value, schema.getType(), schema.getFormat());
      }
    } catch (NumberFormatException e) {
      throw parameterValidationException("Could not cast scalar value [{}] with schema type [{}] and format {}", value,
          schema.getType(), schema.getFormat(), e);
    }
  }

  public static ImmutableList<Object> castList(@NonNull List<String> list, @NonNull Schema<?> schema) {
    return list.stream()
        .map(i -> cast(i, schema))
        .collect(collectingAndThen(toList(), ImmutableList::copyOf));
  }

  public static ImmutableList<Object> castArray(@NonNull String[] array, @NonNull Schema<?> schema) {
    return castList(Arrays.asList(array), schema);
  }

  private static Object castNumber(String value, String format) {
    if ("float".equals(format)) {
      return Float.parseFloat(value);
    } else if ("double".equals(format)) {
      return Double.parseDouble(value);
    } else if (format == null) {
      return new BigDecimal(value);
    } else {
      throw illegalArgumentException("Unsupported format [{}] for number type", format);
    }
  }

  private static Object castInteger(String value, String format) {
    if ("int64".equals(format)) {
      return Long.parseLong(value);
    } else if ("int32".equals(format)) {
      return Integer.parseInt(value);
    } else if (format == null) {
      return new BigInteger(value);
    } else {
      throw illegalArgumentException("Unsupported format [{}] for integer type", format);
    }
  }
}
