package org.dotwebstack.framework.frontend.openapi.handlers;

import lombok.NonNull;

public class RequestHandlerRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 2535875691413184023L;

  public RequestHandlerRuntimeException(@NonNull String message) {
    super(message);
  }

  public RequestHandlerRuntimeException(@NonNull String message, @NonNull Exception cause) {
    super(message, cause);
  }

  public RequestHandlerRuntimeException(@NonNull Exception cause) {
    super(cause);
  }

}
