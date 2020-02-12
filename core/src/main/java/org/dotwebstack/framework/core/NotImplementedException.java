package org.dotwebstack.framework.core;

import lombok.NonNull;

public class NotImplementedException extends DotWebStackRuntimeException {

  private static final long serialVersionUID = 7760665084527035669L;

  public NotImplementedException(@NonNull String message, Throwable cause, Object... arguments) {
    super(message, cause, arguments);
  }
}
