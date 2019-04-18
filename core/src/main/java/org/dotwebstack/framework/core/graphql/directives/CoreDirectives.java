package org.dotwebstack.framework.core.graphql.directives;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class CoreDirectives {

  public static final String TRANSFORM_NAME;

  public static final String TRANSFORM_ARG_EXPR;

  static {
    TRANSFORM_NAME = "transform";
    TRANSFORM_ARG_EXPR = "expr";
  }

}
