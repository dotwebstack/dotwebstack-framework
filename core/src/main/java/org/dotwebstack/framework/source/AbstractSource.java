package org.dotwebstack.framework.source;

import org.dotwebstack.framework.backend.Backend;

public abstract class AbstractSource implements Source {

  protected Backend backend;

  public AbstractSource(Backend backend) {
    this.backend = backend;
  }

  @Override
  public Backend getBackend() {
    return backend;
  }

}
