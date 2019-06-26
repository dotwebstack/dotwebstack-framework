package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class LongConverter extends LiteralConverter<Long> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.LONG.equals(literal.getDatatype());
  }

  @Override
  public Long convertLiteral(@NonNull Literal literal) {
    return literal.longValue();
  }
}
