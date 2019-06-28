package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DoubleConverter extends LiteralConverter<Double> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DOUBLE.equals(literal.getDatatype());
  }

  @Override
  public Double convertLiteral(@NonNull Literal literal) {
    return literal.doubleValue();
  }
}
