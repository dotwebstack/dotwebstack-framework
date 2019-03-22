package org.dotwebstack.framework.core.graphql;

public final class CoreDirectives {

  public static final String SOURCE_NAME = "source";

  private CoreDirectives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", CoreDirectives.class));
  }

}
