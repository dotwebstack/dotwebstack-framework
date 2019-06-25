package org.dotwebstack.framework.backend.rdf4j.converters;

import java.time.LocalDate;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DateConverter extends LiteralConverter<LocalDate> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.DATE.equals(literal.getDatatype());
  }

  @Override
  public LocalDate convertLiteral(Literal literal) {
    return LocalDate.parse(literal.stringValue());
  }
}
