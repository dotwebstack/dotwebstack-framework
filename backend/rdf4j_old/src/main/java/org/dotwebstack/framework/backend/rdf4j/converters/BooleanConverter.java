package org.dotwebstack.framework.backend.rdf4j.converters;

import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class BooleanConverter extends LiteralConverter<Boolean> {

  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.BOOLEAN.equals(literal.getDatatype());
  }

  public Boolean convertLiteral(@NonNull Literal literal) {
    return literal.booleanValue();
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return Boolean.class.getSimpleName()
        .equals(typeAsString);
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    return valueFactory.createLiteral((Boolean) value);
  }
}
