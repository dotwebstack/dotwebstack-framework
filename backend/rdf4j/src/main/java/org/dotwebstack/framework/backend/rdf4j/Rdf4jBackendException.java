package org.dotwebstack.framework.backend.rdf4j;

public class Rdf4jBackendException extends RuntimeException {

  public Rdf4jBackendException(String message) {
    super(message);
  }

  public Rdf4jBackendException(String message, Throwable cause) {
    super(message, cause);
  }

}
