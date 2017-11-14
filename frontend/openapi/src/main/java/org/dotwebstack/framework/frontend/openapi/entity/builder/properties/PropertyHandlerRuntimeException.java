package org.dotwebstack.framework.frontend.openapi.entity.builder.properties;


public class PropertyHandlerRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -4414384722293239982L;

  public PropertyHandlerRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public PropertyHandlerRuntimeException(String message) {
    super(message);
  }

}
