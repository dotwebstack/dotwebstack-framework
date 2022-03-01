package org.dotwebstack.framework.core;

import lombok.NonNull;

public class RequestValidationException extends DotWebStackRuntimeException {
  private static final long serialVersionUID = 7760665084527035669L;

  public RequestValidationException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }
}
