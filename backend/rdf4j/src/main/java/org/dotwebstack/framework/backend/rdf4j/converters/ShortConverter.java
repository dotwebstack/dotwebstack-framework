package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class ShortConverter extends LiteralConverter<Short> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.SHORT.equals(literal.getDatatype());
  }

  @Override
  public Short convertLiteral(@NonNull Literal literal) {
    return literal.shortValue();
  }

}
