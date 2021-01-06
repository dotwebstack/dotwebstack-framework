package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.notImplementedException;

import java.time.LocalDate;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(Rdf4jProperties.class)
public class LocalDateConverter extends LiteralConverter<LocalDate> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATE.equals(literal.getDatatype());
  }

  @Override
  public LocalDate convertLiteral(@NonNull Literal literal) {
    return literal.calendarValue()
        .toGregorianCalendar()
        .toZonedDateTime()
        .toLocalDate();
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
