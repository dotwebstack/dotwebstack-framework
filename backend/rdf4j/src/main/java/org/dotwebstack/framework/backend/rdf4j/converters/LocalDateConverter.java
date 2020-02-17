package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.notImplementedException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
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

  private Rdf4jProperties rdf4jProperties;

  private DateTimeFormatter dateTimeFormatter;

  public LocalDateConverter(Rdf4jProperties rdf4jProperties) {
    this.rdf4jProperties = rdf4jProperties;
    this.dateTimeFormatter = getDateTimeFormatter();
  }

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATE.equals(literal.getDatatype());
  }

  @Override
  public LocalDate convertLiteral(@NonNull Literal literal) {
    return LocalDate.parse(literal.stringValue(), dateTimeFormatter);
  }

  private DateTimeFormatter getDateTimeFormatter() {
    if (Objects.nonNull(rdf4jProperties.getDateproperties()) && Objects.nonNull(rdf4jProperties.getDateproperties()
        .getDateformat())) {
      return DateTimeFormatter.ofPattern(rdf4jProperties.getDateproperties()
          .getDateformat());
    }
    return DateTimeFormatter.ISO_LOCAL_DATE;
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
