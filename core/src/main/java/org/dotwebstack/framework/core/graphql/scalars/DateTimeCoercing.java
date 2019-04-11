package org.dotwebstack.framework.core.graphql.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

class DateTimeCoercing implements Coercing<ZonedDateTime, ZonedDateTime> {

  @Override
  public ZonedDateTime serialize(@NonNull Object o) {
    String dateTimeStr;

    if (o instanceof String) {
      dateTimeStr = (String) o;
    } else if (o instanceof Literal && XMLSchema.DATETIME.equals(((Literal) o).getDatatype())) {
      dateTimeStr = ((Literal) o).stringValue();
    } else {
      throw new CoercingSerializeException(String
          .format("Unable to parse date-time string from '%s' type.", o.getClass().getName()));
    }

    try {
      return ZonedDateTime.parse(dateTimeStr);
    } catch (DateTimeParseException e) {
      throw new CoercingSerializeException("Parsing date-time string failed.", e);
    }
  }

  @Override
  public ZonedDateTime parseValue(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ZonedDateTime parseLiteral(Object o) {
    throw new UnsupportedOperationException();
  }

}
