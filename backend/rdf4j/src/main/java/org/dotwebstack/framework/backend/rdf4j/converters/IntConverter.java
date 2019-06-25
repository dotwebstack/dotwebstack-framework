package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class IntConverter extends LiteralConverter<Integer> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.INT.equals(literal.getDatatype());
  }

  @Override
  public Integer convertLiteral(Literal literal) {
    return literal.intValue();
  }
}
