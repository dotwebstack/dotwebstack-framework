package org.dotwebstack.framework.backend;

public final class BackendException extends RuntimeException {

  private static final long serialVersionUID = 1804832362825170962L;

  public BackendException(String message) {
    super(message);
  }

  public BackendException(String message, Throwable cause) {
    super(message, cause);
  }

}
