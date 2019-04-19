package org.dotwebstack.framework.core.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

class DateTimeCoercing implements Coercing<ZonedDateTime, ZonedDateTime> {

  @Override
  public ZonedDateTime serialize(@NonNull Object value) {
    if (value instanceof ZonedDateTime) {
      return (ZonedDateTime) value;
    }

    String dateTimeStr;

    if (value instanceof String) {
      dateTimeStr = (String) value;
    } else if (value instanceof Literal && XMLSchema.DATETIME
        .equals(((Literal) value).getDatatype())) {
      dateTimeStr = ((Literal) value).stringValue();
    } else {
      throw new CoercingSerializeException(String
          .format("Unable to parse date-time string from '%s' type.", value.getClass().getName()));
    }

    try {
      return ZonedDateTime.parse(dateTimeStr);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date-time string failed.", e);
    }
  }

  @Override
  public ZonedDateTime parseValue(@NonNull Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ZonedDateTime parseLiteral(@NonNull Object value) {
    throw new UnsupportedOperationException();
  }

}
