package org.dotwebstack.framework.core.graphql.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

class DateCoercing implements Coercing<LocalDate, LocalDate> {

  @Override
  public LocalDate serialize(Object o) {
    if (!(o instanceof String)) {
      throw new CoercingSerializeException("Parsing date string failed.");
    }

    try {
      return LocalDate.parse((String) o);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date string failed.", e);
    }
  }

  @Override
  public LocalDate parseValue(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate parseLiteral(Object o) {
    throw new UnsupportedOperationException();
  }

}
