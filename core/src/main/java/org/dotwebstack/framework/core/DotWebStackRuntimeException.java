package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.findCause;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.formatMessage;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.joinArguments;

import lombok.NonNull;

public class DotWebStackRuntimeException extends RuntimeException {

  static final long serialVersionUID = 8207318213215092071L;

  public DotWebStackRuntimeException(@NonNull String message,
                                     Object... arguments) {
    super(formatMessage(message, arguments), findCause(arguments));
  }

  DotWebStackRuntimeException(@NonNull String message,
                              Throwable cause,
                              Object... arguments) {
    this(message, joinArguments(cause, arguments));
  }

}
