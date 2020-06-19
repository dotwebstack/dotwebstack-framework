package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(Rdf4jProperties.class)
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
    if (value instanceof LocalDate) {
      return valueFactory.createLiteral(convertToDateViaInstant((LocalDate) value));
    }
    return valueFactory.createLiteral((Date) value);
  }

  private Date convertToDateViaInstant(LocalDate dateToConvert) {
    return Date.from(dateToConvert.atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant());
  }
}
