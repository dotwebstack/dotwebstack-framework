package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractParameter implements Parameter {

  private final IRI identifier;

  private final String name;

  protected AbstractParameter(@NonNull IRI identifier, @NonNull String name) {
    this.identifier = identifier;
    this.name = name;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public String getName() {
    return name;
  }

}
