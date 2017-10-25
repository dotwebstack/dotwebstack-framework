package org.dotwebstack.framework.filter;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractFilter implements Filter {

  private final IRI identifier;

  private final String name;

  protected AbstractFilter(@NonNull IRI identifier, @NonNull String name) {
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
