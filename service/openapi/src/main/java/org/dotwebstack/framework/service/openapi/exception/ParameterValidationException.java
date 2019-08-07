package org.dotwebstack.framework.service.openapi.exception;

public class ParameterValidationException extends Exception {

  static final long serialVersionUID = 1564735180022L;

  public ParameterValidationException(String formatMessage) {
    super(formatMessage);
  }

  public ParameterValidationException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
