package org.dotwebstack.framework.core.directives;

public final class CoreDirectives {

  public static final String TRANSFORM_NAME;

  public static final String TRANSFORM_ARG_EXPR;

  public static final String CONSTRAINT_NAME;

  public static final String CONSTRAINT_ARG_MIN;

  public static final String CONSTRAINT_ARG_MAX;

  public static final String CONSTRAINT_ARG_ONEOF;

  private CoreDirectives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", CoreDirectives.class));
  }

  static {
    TRANSFORM_NAME = "transform";
    TRANSFORM_ARG_EXPR = "expr";

    CONSTRAINT_NAME = "constraint";
    CONSTRAINT_ARG_MIN = "min";
    CONSTRAINT_ARG_MAX = "max";
    CONSTRAINT_ARG_ONEOF = "oneOf";
  }

}
