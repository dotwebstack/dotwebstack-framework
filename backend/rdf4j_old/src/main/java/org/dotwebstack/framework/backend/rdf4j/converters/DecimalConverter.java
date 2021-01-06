package org.dotwebstack.framework.backend.rdf4j.converters;

import java.math.BigDecimal;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DecimalConverter extends LiteralConverter<BigDecimal> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DECIMAL.equals(literal.getDatatype());
  }

  public BigDecimal convertLiteral(@NonNull Literal literal) {
    return literal.decimalValue();
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return BigDecimal.class.getSimpleName()
        .equals(typeAsString);
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    return valueFactory.createLiteral((BigDecimal) value);
  }
}
