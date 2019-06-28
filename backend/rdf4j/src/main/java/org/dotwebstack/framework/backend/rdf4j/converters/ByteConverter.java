package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class ByteConverter extends LiteralConverter<Byte> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.BYTE.equals(literal.getDatatype());
  }

  @Override
  public Byte convertLiteral(@NonNull Literal literal) {
    return literal.byteValue();
  }
}
