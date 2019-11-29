package org.dotwebstack.framework.core.scalars;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

@Component
class DateTimeCoercing implements Coercing<ZonedDateTime, ZonedDateTime> {

  @Override
  public ZonedDateTime serialize(@NonNull Object value) {
    if (value instanceof ZonedDateTime) {
      return (ZonedDateTime) value;
    }

    if (!(value instanceof String)) {
      throw new CoercingSerializeException(
          String.format("Unable to parse date-time string from '%s' type.", value.getClass()
              .getName()));
    }

    try {
      return ZonedDateTime.parse((String) value);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date-time string failed.", e);
    }
  }

  @Override
  public ZonedDateTime parseValue(@NonNull Object value) {
    return serialize(value);
  }

  @Override
  public ZonedDateTime parseLiteral(@NonNull Object value) {
    if (value instanceof StringValue) {
      StringValue stringValue = (StringValue) value;
      if (Objects.equals("NOW", stringValue.getValue())) {
        return ZonedDateTime.now();
      }
    }

    throw unsupportedOperationException("Parsing of literal {} is not supported!",value);
  }
}
