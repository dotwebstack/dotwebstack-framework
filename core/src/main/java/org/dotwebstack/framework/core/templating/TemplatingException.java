package org.dotwebstack.framework.core.templating;

import org.dotwebstack.framework.core.DotWebStackRuntimeException;

public class TemplatingException extends DotWebStackRuntimeException {

  private static final long serialVersionUID = 1L;

  public TemplatingException(String message, Throwable exception) {
    super(message, exception);
  }
}
