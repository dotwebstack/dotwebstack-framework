package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.Date;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Component;

@Component
public class DateConverter extends LiteralConverter<Date> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return false;
  }

  @Override
  public Date convertLiteral(@NonNull Literal literal) {
    throw unsupportedOperationException("Use the LocalDate converter!");
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return Date.class.getSimpleName()
        .equals(typeAsString);
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    return valueFactory.createLiteral((Date) value);
  }
}
