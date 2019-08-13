package org.dotwebstack.framework.service.openapi.exception;

public class ParameterValidationException extends RuntimeException {

  static final long serialVersionUID = 1564735180022L;

  ParameterValidationException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
