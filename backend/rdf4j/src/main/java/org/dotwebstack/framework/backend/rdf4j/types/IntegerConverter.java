package org.dotwebstack.framework.backend.rdf4j.types;

import java.math.BigInteger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
class IntegerConverter implements LiteralConverter<BigInteger> {

  @Override
  public BigInteger convert(Literal value) {
    return value.integerValue();
  }

  @Override
  public boolean supports(IRI dataType) {
    return XMLSchema.INTEGER.equals(dataType);
  }

}
