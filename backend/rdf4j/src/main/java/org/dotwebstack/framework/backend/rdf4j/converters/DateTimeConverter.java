package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.notImplementedException;

import java.time.ZonedDateTime;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DateTimeConverter extends LiteralConverter<ZonedDateTime> {

  public DateTimeConverter() {}

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATETIME.equals(literal.getDatatype());
  }

  @Override
  public ZonedDateTime convertLiteral(@NonNull Literal literal) {
    return literal.calendarValue()
        .toGregorianCalendar()
        .toZonedDateTime();
  }

  @Override
  public boolean supportsType(@Nonnull String type) {
    return false;
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    throw notImplementedException("Converting value for DateTime is not implemented.");
  }
}
