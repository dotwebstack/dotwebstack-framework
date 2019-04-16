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
  public LocalDate serialize(@NonNull Object o) {
    String dateStr;

    if (o instanceof String) {
      dateStr = (String) o;
    } else if (o instanceof Literal && XMLSchema.DATE.equals(((Literal) o).getDatatype())) {
      dateStr = ((Literal) o).stringValue();
    } else {
      throw new CoercingSerializeException(
          String.format("Unable to parse date string from '%s' type.", o.getClass().getName()));
    }

    try {
      return LocalDate.parse(dateStr);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date string failed.", e);
    }
  }

  @Override
  public LocalDate parseValue(@NonNull Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate parseLiteral(@NonNull Object o) {
    throw new UnsupportedOperationException();
  }

}
