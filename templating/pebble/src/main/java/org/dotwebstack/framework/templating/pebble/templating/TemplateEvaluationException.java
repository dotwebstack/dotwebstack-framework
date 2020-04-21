package org.dotwebstack.framework.templating.pebble.templating;

import org.dotwebstack.framework.core.templating.TemplatingException;

public class TemplateEvaluationException extends TemplatingException {
  private static final long serialVersionUID = 1L;

  public TemplateEvaluationException(String message, Throwable exception) {
    super(message, exception);
  }
}
