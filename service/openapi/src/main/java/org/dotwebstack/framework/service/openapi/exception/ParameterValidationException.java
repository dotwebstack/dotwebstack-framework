package org.dotwebstack.framework.service.openapi.exception;

public class ParameterValidationException extends Exception {

  public ParameterValidationException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
