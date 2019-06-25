package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class BooleanConverter extends LiteralConverter<Boolean> {

  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.BOOLEAN.equals(literal.getDatatype());
  }

  public Boolean convertLiteral(Literal literal) {
    return literal.booleanValue();
  }
}
