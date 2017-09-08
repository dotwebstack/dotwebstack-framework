package org.dotwebstack.framework.frontend.openapi;

public class EntityBuilderRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -5903820182842685076L;

  public EntityBuilderRuntimeException(String message) {
    super(message);
  }

  public EntityBuilderRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}
