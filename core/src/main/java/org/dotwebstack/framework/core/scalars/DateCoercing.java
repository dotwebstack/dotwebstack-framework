package org.dotwebstack.framework.core.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.NonNull;

class DateCoercing implements Coercing<LocalDate, LocalDate> {

  @Override
  public LocalDate serialize(@NonNull Object value) {
    if (value instanceof LocalDate) {
      return (LocalDate) value;
    }

    if (!(value instanceof String)) {
      throw new CoercingSerializeException(String.format("Unable to parse date string from '%s' type.", value.getClass()
          .getName()));
    }

    try {
      return LocalDate.parse((String) value);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date string failed.", e);
    }
  }

  @Override
  public LocalDate parseValue(@NonNull Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate parseLiteral(@NonNull Object value) {
    throw new UnsupportedOperationException();
  }

}
