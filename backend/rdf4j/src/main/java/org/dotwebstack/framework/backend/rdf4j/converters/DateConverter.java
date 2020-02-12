package org.dotwebstack.framework.backend.rdf4j.converters;

import java.util.Date;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.apache.http.client.utils.DateUtils;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class DateConverter extends LiteralConverter<Date> {

  @Override
  public boolean supportsLiteral(@NonNull Literal literal) {
    return XMLSchema.DATE.equals(literal.getDatatype());
  }

  @Override
  public Date convertLiteral(@NonNull Literal literal) {
    return DateUtils.parseDate(literal.stringValue());
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
