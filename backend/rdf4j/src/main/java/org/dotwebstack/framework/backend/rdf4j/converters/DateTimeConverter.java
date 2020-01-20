package org.dotwebstack.framework.backend.rdf4j.converters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DateTimeConverter extends LiteralConverter<ZonedDateTime> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATETIME.equals(literal.getDatatype());
  }

  @Override
  public ZonedDateTime convertLiteral(@NonNull Literal literal) {
    try {
      return ZonedDateTime.parse(literal.stringValue());
    } catch (DateTimeParseException ex) {
      return ZonedDateTime.of(LocalDateTime.parse(literal.stringValue()), ZoneId.of("Europe/Amsterdam"));
    }
  }

}
