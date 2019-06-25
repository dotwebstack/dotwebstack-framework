package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DoubleConverter extends LiteralConverter<Double> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.DOUBLE.equals(literal.getDatatype());
  }

  @Override
  public Double convertLiteral(Literal literal) {
    return literal.doubleValue();
  }
}
