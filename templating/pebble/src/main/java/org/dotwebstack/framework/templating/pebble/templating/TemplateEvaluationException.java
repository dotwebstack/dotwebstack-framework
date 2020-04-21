package org.dotwebstack.framework.templating.pebble.templating;

import org.dotwebstack.framework.core.templating.TemplatingException;

public class TemplateEvaluationException extends TemplatingException {
  public TemplateEvaluationException(String message, Throwable exception) {
    super(message, exception);
  }
}
