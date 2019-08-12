package org.dotwebstack.framework.core.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import lombok.NonNull;
import org.springframework.stereotype.Component;

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
    throw new UnsupportedOperationException();
  }
}
