package org.dotwebstack.framework.backend.rdf4j.converters;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class IntegerConverter extends LiteralConverter<BigInteger> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.INTEGER.equals(literal.getDatatype());
  }

  @Override
  public BigInteger convertLiteral(@NonNull Literal literal) {
    return literal.integerValue();
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return BigInteger.class.getSimpleName()
        .equals(typeAsString);
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    return valueFactory.createLiteral((BigInteger) value);
  }
}
