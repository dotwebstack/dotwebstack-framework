package org.dotwebstack.framework.backend;

import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractBackend implements Backend {

  protected IRI identifier;

  public AbstractBackend(IRI identifier) {
    this.identifier = identifier;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

}
