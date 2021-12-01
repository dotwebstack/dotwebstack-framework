package org.dotwebstack.framework.service.openapi.exception;

public class BadRequestException extends RuntimeException {

  static final long serialVersionUID = 1564735180022L;

  BadRequestException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }

}
