package org.dotwebstack.framework.frontend.openapi.entity;

class LdPathExecutorRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 7089325450909214388L;

  public LdPathExecutorRuntimeException(String message) {
    super(message);
  }

  public LdPathExecutorRuntimeException(String message, Exception re) {
    super(message, re);
  }
}
