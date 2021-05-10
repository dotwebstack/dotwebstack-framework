package org.dotwebstack.framework.core.scalars;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
class DateTimeCoercing implements Coercing<OffsetDateTime, OffsetDateTime> {

  @Override
  public OffsetDateTime serialize(@NonNull Object value) {
    if (value instanceof OffsetDateTime) {
      return (OffsetDateTime) value;
    }

    if (!(value instanceof String)) {
      throw new CoercingSerializeException(
          String.format("Unable to parse date-time string from '%s' type.", value.getClass()
              .getName()));
    }

    try {
      return OffsetDateTime.parse((String) value);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date-time string failed.", e);
    }
  }

  @Override
  public OffsetDateTime parseValue(@NonNull Object value) {
    return serialize(value);
  }

  @Override
  public OffsetDateTime parseLiteral(@NonNull Object value) {
    if (value instanceof StringValue) {
      StringValue stringValue = (StringValue) value;
      if (Objects.equals("NOW", stringValue.getValue())) {
        return OffsetDateTime.now();
      }

      return OffsetDateTime.parse(stringValue.getValue());
    }

    throw unsupportedOperationException("Parsing of literal {} is not supported!", value);
  }
}
