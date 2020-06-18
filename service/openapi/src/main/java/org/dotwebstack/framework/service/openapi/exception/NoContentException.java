package org.dotwebstack.framework.service.openapi.exception;

public class NoContentException extends RuntimeException {

  static final long serialVersionUID = 1564735180022L;

  NoContentException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
