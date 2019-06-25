package org.dotwebstack.framework.backend.rdf4j.converters;

import java.math.BigInteger;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class IntegerConverter extends LiteralConverter<BigInteger> {

  @Override
  public boolean supportsLiteral(Literal literal) {
    return XMLSchema.INTEGER.equals(literal.getDatatype());
  }

  @Override
  public BigInteger convertLiteral(Literal literal) {
    return literal.integerValue();
  }
}
