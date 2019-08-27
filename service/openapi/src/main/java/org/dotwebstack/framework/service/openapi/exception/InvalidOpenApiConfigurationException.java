package org.dotwebstack.framework.service.openapi.exception;

import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;

public class InvalidOpenApiConfigurationException extends InvalidConfigurationException {

  private static final long serialVersionUID = 1L;

  public InvalidOpenApiConfigurationException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }

  public InvalidOpenApiConfigurationException(@NonNull String message, Throwable cause, Object... arguments) {
    super(message, cause, arguments);
  }
}
