package org.dotwebstack.framework.service.openapi.exception;

public class NotAcceptableException extends RuntimeException {

  static final long serialVersionUID = 1564735180022L;

  public NotAcceptableException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }

}
