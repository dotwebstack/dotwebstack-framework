package org.dotwebstack.framework.core.directives;

import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public final class CoreDirectives {

  public static final String TRANSFORM_NAME = "transform";

  public static final String TRANSFORM_ARG_EXPR =  "expr";

  public static final String CONSTRAINT_NAME = "constraint";

  public static final String CONSTRAINT_ARG_MIN = "min";

  public static final String CONSTRAINT_ARG_MAX = "max";

  public static final String CONSTRAINT_ARG_ONEOF = "oneOf";

  public static final String CONSTRAINT_ARG_ONEOF_INT = "oneOfInt";


  private CoreDirectives() {
    throw ExceptionHelper.illegalArgumentException(
            "{} is not meant to be instantiated.", CoreDirectives.class);
  }
}
