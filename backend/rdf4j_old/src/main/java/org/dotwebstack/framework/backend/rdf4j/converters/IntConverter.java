package org.dotwebstack.framework.backend.rdf4j.converters;

import java.util.List;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class IntConverter extends LiteralConverter<Integer> {

  private static final List<String> SUPPORTED_TYPES = List.of("Int", Integer.class.getSimpleName());

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.INT.equals(literal.getDatatype());
  }

  @Override
  public Integer convertLiteral(@NonNull Literal literal) {
    return literal.intValue();
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return SUPPORTED_TYPES.contains(typeAsString);
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    return valueFactory.createLiteral((Integer) value);
  }
}
