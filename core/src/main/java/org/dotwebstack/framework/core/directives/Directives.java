package org.dotwebstack.framework.core.directives;

public final class Directives {

  public static final String SOURCE_NAME = "source";

  public static final String SOURCE_ARG_BACKEND = "backend";

  private Directives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Directives.class));
  }

}
