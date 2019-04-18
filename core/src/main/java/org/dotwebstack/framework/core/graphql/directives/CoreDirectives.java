package org.dotwebstack.framework.core.graphql.directives;

public final class CoreDirectives {

  public static final String TRANSFORM_NAME;

  public static final String TRANSFORM_ARG_EXPR;

  private CoreDirectives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", CoreDirectives.class));
  }

  static {
    TRANSFORM_NAME = "transform";
    TRANSFORM_ARG_EXPR = "expr";
  }

}
