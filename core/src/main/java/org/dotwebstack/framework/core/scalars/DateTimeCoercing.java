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
class DateTimeCoercing implements Coercing<DateTimeSupplier, OffsetDateTime> {

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
  public DateTimeSupplier parseValue(@NonNull Object value) {
    return new DateTimeSupplier(false, serialize(value));
  }

  @Override
  public DateTimeSupplier parseLiteral(@NonNull Object value) {
    if (value instanceof StringValue) {
      var stringValue = (StringValue) value;
      if (Objects.equals("NOW", stringValue.getValue())) {
        return new DateTimeSupplier(true);
      }

      return new DateTimeSupplier(false, OffsetDateTime.parse(stringValue.getValue()));
    }

    throw unsupportedOperationException("Parsing of literal {} is not supported!", value);
  }
}
