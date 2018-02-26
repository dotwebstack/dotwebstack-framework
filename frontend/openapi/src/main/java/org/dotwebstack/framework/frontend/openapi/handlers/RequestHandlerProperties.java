package org.dotwebstack.framework.frontend.openapi.handlers;

public final class RequestHandlerProperties {

  public static final String OPERATION = "operation";

  public static final String PATH = "path";

  private RequestHandlerProperties() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", RequestHandlerProperties.class));
  }

}
