package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractParameter<T> implements Parameter<T> {

  private final IRI identifier;

  private final String name;

  private final boolean required;

  protected AbstractParameter(@NonNull IRI identifier, @NonNull String name, boolean required) {
    this.identifier = identifier;
    this.name = name;
    this.required = required;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isRequired() {
    return required;
  }
}
