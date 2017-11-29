package org.dotwebstack.framework.frontend.openapi.entity.backend;

import org.eclipse.rdf4j.repository.RepositoryException;

class Rdf4jBackendRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -3200657648895745521L;

  public Rdf4jBackendRuntimeException(String message, RepositoryException exception) {
    super(message, exception);
  }
}
