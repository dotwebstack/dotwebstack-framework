package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class ShortConverter extends LiteralConverter<Short> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.SHORT.equals(literal.getDatatype());
  }

  @Override
  public Short convertLiteral(Literal literal) {
    return literal.shortValue();
  }

}
