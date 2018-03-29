package org.dotwebstack.framework.param.term;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

public class StringTermParameter extends TermParameter<String> {

  public StringTermParameter(@NonNull IRI identifier, @NonNull String name, boolean required) {
    this(identifier, name, required, null);
  }

  public StringTermParameter(@NonNull Resource identifier, @NonNull String name, boolean required,
      String defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(@NonNull String value) {
    return VALUE_FACTORY.createLiteral(value);
  }

  @Override
  protected String handleInner(@NonNull String value) {
    return value;
  }

}
