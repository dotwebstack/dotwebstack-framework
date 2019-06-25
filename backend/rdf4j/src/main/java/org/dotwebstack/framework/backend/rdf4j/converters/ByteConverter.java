package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class ByteConverter extends LiteralConverter<Byte> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.BOOLEAN.equals(literal.getDatatype());
  }

  @Override
  public Byte convertLiteral(Literal literal) {
    return literal.byteValue();
  }
}
