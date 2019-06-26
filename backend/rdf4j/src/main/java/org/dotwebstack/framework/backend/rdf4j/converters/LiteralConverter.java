package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.Literal;

public abstract class LiteralConverter<O> implements CoreConverter<O> {

  @Override
  public boolean supports(@NonNull Object value) {
    if (!(value instanceof Literal)) {
      return false;
    }

    return supportsLiteral((Literal) value);
  }

  @Override
  public O convert(@NonNull Object value) {
    return convertLiteral((Literal) value);
  }

  public abstract boolean supportsLiteral(@NonNull Literal literal);

  public abstract O convertLiteral(@NonNull Literal literal);

}
