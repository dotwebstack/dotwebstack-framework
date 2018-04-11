package org.dotwebstack.framework.param.term;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

public class IriTermParameter extends TermParameter<IRI> {

  public IriTermParameter(@NonNull Resource identifier, @NonNull String name, boolean required) {
    this(identifier, name, required, null);
  }

  public IriTermParameter(@NonNull Resource identifier, @NonNull String name, boolean required,
      IRI defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public IRI getValue(@NonNull IRI value) {
    return value;
  }

  @Override
  protected IRI handleInner(@NonNull String value) {
    return VALUE_FACTORY.createIRI(value);
  }
}
