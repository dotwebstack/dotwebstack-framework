package org.dotwebstack.framework.core.templating;

public class TemplatingException extends RuntimeException {
  public TemplatingException(String message, Throwable exception) {
    super(message, exception);
  }
}
