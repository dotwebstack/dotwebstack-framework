package org.dotwebstack.framework.config;

public final class ConfigurationException extends RuntimeException {

  private static final long serialVersionUID = -1278504572617670618L;

  public ConfigurationException(String message) {
    super(message);
  }

  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

}
