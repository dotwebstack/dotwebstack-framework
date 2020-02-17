package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.notImplementedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DateTimeConverter extends LiteralConverter<ZonedDateTime> {

  private Rdf4jProperties rdf4jProperties;

  public DateTimeConverter(Rdf4jProperties rdf4jProperties) {
    this.rdf4jProperties = rdf4jProperties;
  }

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATETIME.equals(literal.getDatatype());
  }

  @Override
  public ZonedDateTime convertLiteral(@NonNull Literal literal) {
    DateTimeFormatter dateTimeFormatter = getDatetimeFormatter();

    try {
      return ZonedDateTime.parse(literal.stringValue(), dateTimeFormatter);
    } catch (DateTimeParseException e) {
      ZoneId zoneId = getTimezone();
      return ZonedDateTime.of(LocalDateTime.parse(literal.stringValue(), dateTimeFormatter), zoneId);
    }
  }

  @Override
  public boolean supportsType(@Nonnull String type) {
    return false;
  }

  @Override
  public Value convertToValue(@NonNull Object value) {
    throw notImplementedException("Converting value for DateTime is not implemented.");
  }

  private DateTimeFormatter getDatetimeFormatter() {
    if (Objects.nonNull(rdf4jProperties.getDateproperties()) && Objects.nonNull(rdf4jProperties.getDateproperties()
        .getDatetimeformat())) {
      return DateTimeFormatter.ofPattern(rdf4jProperties.getDateproperties()
          .getDatetimeformat());
    }

    return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  }

  private ZoneId getTimezone() {
    if (Objects.nonNull(rdf4jProperties.getDateproperties()) && Objects.nonNull(rdf4jProperties.getDateproperties()
        .getTimezone())) {
      return ZoneId.of(rdf4jProperties.getDateproperties()
          .getTimezone());
    }
    return ZoneId.systemDefault();
  }
}
