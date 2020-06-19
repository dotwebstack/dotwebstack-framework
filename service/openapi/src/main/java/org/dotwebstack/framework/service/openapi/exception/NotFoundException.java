package org.dotwebstack.framework.service.openapi.exception;

public class NotFoundException extends RuntimeException {

  static final long serialVersionUID = 1564735180022L;

  NotFoundException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
