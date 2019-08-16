package org.dotwebstack.framework.service.openapi.exception;

public class GraphQlErrorException extends Exception {

  static final long serialVersionUID = 1564735180022L;

  GraphQlErrorException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
