package org.dotwebstack.framework.param.term;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class IriTermParameter extends TermParameter<IRI> {

  IriTermParameter(@NonNull IRI identifier, @NonNull String name, boolean required) {
    this(identifier, name, required, null);
  }

  IriTermParameter(@NonNull IRI identifier, @NonNull String name, boolean required,
      IRI defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(@NonNull IRI value) {
    return VALUE_FACTORY.createLiteral(value.stringValue());
  }

  @Override
  protected IRI handleInner(@NonNull String value) {
    return VALUE_FACTORY.createIRI(value);
  }
}
