package org.dotwebstack.framework.backend.rdf4j;

import lombok.NonNull;

public class Rdf4jBackendException extends RuntimeException {

  public Rdf4jBackendException(@NonNull String message) {
    super(message);
  }

  public Rdf4jBackendException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }

}
