package org.dotwebstack.framework.backend.rdf4j.converters;

import java.time.ZonedDateTime;
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
    return ZonedDateTime.parse(literal.stringValue());
  }

}
