package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

public abstract class LiteralConverter<O> implements CoreConverter<Value, O> {

  @Override
  public boolean supports(@NonNull Value value) {
    if (!(value instanceof Literal)) {
      return false;
    }

    return supportsLiteral((Literal) value);
  }

  @Override
  public O convert(@NonNull Value value) {
    return convertLiteral((Literal) value);
  }

  public abstract boolean supportsLiteral(@NonNull Literal literal);

  public abstract O convertLiteral(@NonNull Literal literal);

}
