package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class FloatConverter extends LiteralConverter<Float> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.FLOAT.equals(literal.getDatatype());
  }

  @Override
  public Float convertLiteral(Literal literal) {
    return literal.floatValue();
  }
}
