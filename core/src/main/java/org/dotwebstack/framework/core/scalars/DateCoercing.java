package org.dotwebstack.framework.core.scalars;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DateCoercing implements Coercing<LocalDate, LocalDate> {

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
      return LocalDate.parse(((String)value).substring(0, ((String)value).lastIndexOf('T')));
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date string failed.", e);
    }
  }

  @Override
  public LocalDate parseValue(@NonNull Object value) {
    return serialize(value);
  }

  @Override
  public LocalDate parseLiteral(@NonNull Object value) {
    if (value instanceof StringValue) {
      StringValue stringValue = (StringValue) value;
      if (Objects.equals("NOW", stringValue.getValue())) {
        return LocalDate.now();
      }

      return LocalDate.parse(stringValue.getValue());
    }
    throw unsupportedOperationException("Parsing of literal {} is not supported!", value);
  }

}
