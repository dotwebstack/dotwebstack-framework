package org.dotwebstack.framework.frontend.openapi.entity;

import lombok.NonNull;

class LdPathExecutorRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 7089325450909214388L;

  public LdPathExecutorRuntimeException(@NonNull String message) {
    super(message);
  }

  public LdPathExecutorRuntimeException(@NonNull String message, @NonNull Exception re) {
    super(message, re);
  }
}
