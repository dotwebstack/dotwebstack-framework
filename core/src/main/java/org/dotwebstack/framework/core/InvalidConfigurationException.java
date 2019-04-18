package org.dotwebstack.framework.core;

import lombok.NonNull;

public class InvalidConfigurationException extends RuntimeException {

  private static final long serialVersionUID = -5876935563736110824L;

  public InvalidConfigurationException(@NonNull String message) {
    super(message);
  }

  public InvalidConfigurationException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }

}
