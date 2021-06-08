package org.dotwebstack.framework.core.scalars;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DateCoercing implements Coercing<DateSupplier, LocalDate> {

  @Override
  public LocalDate serialize(@NonNull Object value) {
    if (value instanceof LocalDate) {
      return (LocalDate) value;
    }

    if (value instanceof Date) {
      return ((Date) value).toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDate();
    }

    if (!(value instanceof String)) {
      throw new CoercingSerializeException(String.format("Unable to parse date string from '%s' type.", value.getClass()
          .getName()));
    }

    try {
      // to be able to also convert dates that also contain times
      if (((String) value).contains("T")) {
        return getLocalDateFromDateTimeString((String) value);
      }

      return LocalDate.parse((String) value);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date string failed.", e);
    }
  }

  private LocalDate getLocalDateFromDateTimeString(@NonNull String value) {
    try {
      var zonedDateTime = ZonedDateTime.parse(value);
      return zonedDateTime.toLocalDate();
    } catch (DateTimeParseException e) {
      var localDateTime = LocalDateTime.parse(value);
      return localDateTime.toLocalDate();
    }
  }

  @Override
  public DateSupplier parseValue(@NonNull Object value) {
    return new DateSupplier(false, serialize(value));
  }

  @Override
  public DateSupplier parseLiteral(@NonNull Object value) {
    if (value instanceof StringValue) {
      var stringValue = (StringValue) value;
      if (Objects.equals("NOW", stringValue.getValue())) {
        return new DateSupplier(true, null);
      }

      return new DateSupplier(false, LocalDate.parse(stringValue.getValue()));
    }
    throw unsupportedOperationException("Parsing of literal {} is not supported!", value);
  }

}
