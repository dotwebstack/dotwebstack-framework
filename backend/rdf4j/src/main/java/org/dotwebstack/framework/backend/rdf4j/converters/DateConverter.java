package org.dotwebstack.framework.backend.rdf4j.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

@Component
public class DateConverter extends LiteralConverter<Date> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATE.equals(literal.getDatatype());
  }

  @Override
  public Date convertLiteral(@NonNull Literal literal) {
    try {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      return format.parse(literal.stringValue());
    } catch (ParseException e) {
      throw illegalArgumentException("Format for date {} is incorrect", literal);
    }
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
