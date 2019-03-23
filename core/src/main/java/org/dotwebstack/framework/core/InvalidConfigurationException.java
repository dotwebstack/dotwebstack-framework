package org.dotwebstack.framework.core;

import lombok.NonNull;

public class InvalidConfigurationException extends RuntimeException {

  public InvalidConfigurationException(@NonNull String message) {
    super(message);
  }

  public InvalidConfigurationException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }

}
