package org.dotwebstack.framework.core.graphql.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

class DateCoercing implements Coercing<LocalDate, LocalDate> {

  @Override
  public LocalDate serialize(@NonNull Object value) {
    if (value instanceof LocalDate) {
      return (LocalDate) value;
    }

    String dateStr;

    if (value instanceof String) {
      dateStr = (String) value;
    } else if (value instanceof Literal && XMLSchema.DATE.equals(((Literal) value).getDatatype())) {
      dateStr = ((Literal) value).stringValue();
    } else {
      throw new CoercingSerializeException(
          String.format("Unable to parse date string from '%s' type.", value.getClass().getName()));
    }

    try {
      return LocalDate.parse(dateStr);
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
