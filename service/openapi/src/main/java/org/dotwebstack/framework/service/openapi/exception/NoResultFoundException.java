package org.dotwebstack.framework.service.openapi.exception;

public class NoResultFoundException extends Exception {

  static final long serialVersionUID = 1564735180022L;

  NoResultFoundException(String formatMessage, Throwable cause) {
    super(formatMessage, cause);
  }
}
