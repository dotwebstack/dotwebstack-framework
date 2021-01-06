package org.dotwebstack.framework.backend.rdf4j.converters;

import java.util.List;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class StringConverter extends LiteralConverter<String> {

  private static final List<String> SUPPORTED_TYPES = List.of("ID", String.class.getSimpleName());

  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.STRING.equals(literal.getDatatype());
  }

  public String convertLiteral(@NonNull Literal literal) {
    return literal.stringValue();
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return SUPPORTED_TYPES.contains(typeAsString);
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    return valueFactory.createLiteral((String) value);
  }
}
