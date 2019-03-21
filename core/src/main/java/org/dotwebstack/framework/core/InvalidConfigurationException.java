package org.dotwebstack.framework.core;

public class InvalidConfigurationException extends RuntimeException {

  public InvalidConfigurationException(String message) {
    super(message);
  }

  public InvalidConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

}
