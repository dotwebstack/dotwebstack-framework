package org.dotwebstack.framework.backend.rdf4j.converters;

import java.time.ZonedDateTime;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DateTimeConverter extends LiteralConverter<ZonedDateTime> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.DATETIME.equals(literal.getDatatype());
  }

  @Override
  public ZonedDateTime convertLiteral(Literal literal) {
    return ZonedDateTime.parse(literal.stringValue());
  }

}
