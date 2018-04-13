package org.dotwebstack.framework.param.term;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

public class BooleanTermParameter extends TermParameter<Boolean> {

  public BooleanTermParameter(@NonNull Resource identifier, @NonNull String name,
      boolean required) {
    this(identifier, name, required, null);
  }

  public BooleanTermParameter(@NonNull Resource identifier, @NonNull String name, boolean required,
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
