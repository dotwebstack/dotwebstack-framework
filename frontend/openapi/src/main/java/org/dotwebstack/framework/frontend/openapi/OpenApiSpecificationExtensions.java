package org.dotwebstack.framework.frontend.openapi;

public final class OpenApiSpecificationExtensions {

  public static final String DOTWEBSTACK_PREFIX = "x-dotwebstack-";

  public static final String INFORMATION_PRODUCT = DOTWEBSTACK_PREFIX.concat("information-product");

  public static final String PARAMETER_INPUT = DOTWEBSTACK_PREFIX.concat("parameter-input");

  private OpenApiSpecificationExtensions() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", OpenApiSpecificationExtensions.class));
  }

}
