package org.dotwebstack.framework.core.scalars;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.DEPRECATED_METHOD_ERROR_TEXT;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
class DateTimeCoercing implements Coercing<DateTimeSupplier, OffsetDateTime> {

  @Override
  @SuppressWarnings("deprecation")
  public OffsetDateTime serialize(@NonNull Object value) {
    throw unsupportedOperationException(DEPRECATED_METHOD_ERROR_TEXT);
  }

  @Override
  public OffsetDateTime serialize(@NonNull Object value, @NonNull GraphQLContext context, @NonNull Locale locale) {
    if (value instanceof OffsetDateTime offsetDateTime) {
      return offsetDateTime;
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
  @SuppressWarnings("deprecation")
  public DateTimeSupplier parseValue(@NonNull Object value) {
    throw unsupportedOperationException(DEPRECATED_METHOD_ERROR_TEXT);
  }

  @Override
  public DateTimeSupplier parseValue(@NonNull Object value, @NonNull GraphQLContext context, @NonNull Locale locale) {
    return new DateTimeSupplier(false, serialize(value, context, locale));
  }

  @Override
  @SuppressWarnings("deprecation")
  public DateTimeSupplier parseLiteral(@NonNull Object value) {
    throw unsupportedOperationException(DEPRECATED_METHOD_ERROR_TEXT);
  }

  @Override
  public DateTimeSupplier parseLiteral(@NonNull Value<?> value, @NonNull CoercedVariables variables,
      @NonNull GraphQLContext context, @NonNull Locale locale) {
    if (value instanceof StringValue stringValue) {
      if (Objects.equals("NOW", stringValue.getValue())) {
        return new DateTimeSupplier(true);
      }

      return new DateTimeSupplier(false, OffsetDateTime.parse(stringValue.getValue()));
    }

    throw unsupportedOperationException("Parsing of literal {} is not supported!", value);
  }
}
