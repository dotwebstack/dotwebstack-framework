package org.dotwebstack.framework.backend.rdf4j.converters;

import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
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

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return Byte.class.getSimpleName()
        .equals(typeAsString);
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    return valueFactory.createLiteral((Byte) value);
  }
}
