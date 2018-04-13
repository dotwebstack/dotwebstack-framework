package org.dotwebstack.framework.param.term;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

public class IntegerTermParameter extends TermParameter<Integer> {

  public IntegerTermParameter(@NonNull Resource identifier, @NonNull String name,
      boolean required) {
    this(identifier, name, required, null);
  }

  public IntegerTermParameter(@NonNull Resource identifier, @NonNull String name, boolean required,
      Integer defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(@NonNull Integer value) {
    return VALUE_FACTORY.createLiteral(value);
  }

  @Override
  protected Integer handleInner(@NonNull String value) {
    return Integer.parseInt(value);
  }
}
