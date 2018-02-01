package org.dotwebstack.framework.param.term;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class BooleanTermParameter extends TermParameter<Boolean> {

  BooleanTermParameter(@NonNull IRI identifier, @NonNull String name, boolean required) {
    this(identifier, name, required, null);
  }

  BooleanTermParameter(@NonNull IRI identifier, @NonNull String name, boolean required,
      Boolean defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(@NonNull Boolean value) {
    return VALUE_FACTORY.createLiteral(value);
  }

  @Override
  protected Boolean handleInner(@NonNull String value) {
    return Boolean.valueOf(value);
  }
}
