package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.notImplementedException;

import java.time.LocalDate;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class LocalDateConverter extends LiteralConverter<LocalDate> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATE.equals(literal.getDatatype());
  }

  @Override
  public LocalDate convertLiteral(@NonNull Literal literal) {
    return LocalDate.parse(literal.stringValue());
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return false;
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    throw notImplementedException("Converting value for LocalDate is not implemented.");
  }
}
