package org.dotwebstack.framework.service.openapi.mapping;

public class MappingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MappingException(String message) {
    super(message);
  }

  public MappingException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
