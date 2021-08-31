package org.dotwebstack.framework.service.openapi.param;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
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

  private ParamValueCaster() {}

  public static Object cast(String value, @NonNull Schema<?> schema) {
    if (value == null) {
      return null;
    }
    if ("string".equals(schema.getType())) {
      return value;
    }
    try {
      if (NUMBER_TYPE.equals(schema.getType()) && "float".equals(schema.getFormat())) {
        return Float.parseFloat(value);
      } else if (NUMBER_TYPE.equals(schema.getType()) && "double".equals(schema.getFormat())) {
        return Double.parseDouble(value);
      } else if (NUMBER_TYPE.equals(schema.getType()) && schema.getFormat() == null) {
        return new BigDecimal(value);
      } else if ("integer".equals(schema.getType()) && "int64".equals(schema.getFormat())) {
        return Long.parseLong(value);
      } else if ("integer".equals(schema.getType()) && "int32".equals(schema.getFormat())) {
        return Integer.parseInt(value);
      } else if ("integer".equals(schema.getType()) && schema.getFormat() == null) {
        return new BigInteger(value);
      } else if ("boolean".equals(schema.getType())) {
        return Boolean.parseBoolean(value);
      } else {
        throw illegalArgumentException("Could not cast scalar value [{}] with schema type [{}] " + "and format {}",
            value, schema.getType(), schema.getFormat());
      }
    } catch (NumberFormatException e) {
      throw parameterValidationException("Could not cast scalar value [{}] with schema type [{}] " + "and format {}",
          value, schema.getType(), schema.getFormat(), e);
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
}
