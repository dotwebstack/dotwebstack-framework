package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class LongConverter extends LiteralConverter<Long> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.LONG.equals(literal.getDatatype());
  }

  @Override
  public Long convertLiteral(Literal literal) {
    return literal.longValue();
  }
}
