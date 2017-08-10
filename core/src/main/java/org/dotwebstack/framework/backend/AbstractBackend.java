package org.dotwebstack.framework.backend;

import java.util.Objects;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractBackend implements Backend {

  protected IRI identifier;

  public AbstractBackend(IRI identifier) {
    this.identifier = Objects.requireNonNull(identifier);
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

}
