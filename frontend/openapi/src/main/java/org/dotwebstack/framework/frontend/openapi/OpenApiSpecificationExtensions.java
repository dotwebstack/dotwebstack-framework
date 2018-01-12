package org.dotwebstack.framework.frontend.openapi;

public final class OpenApiSpecificationExtensions {

  public static final String DOTWEBSTACK_PREFIX = "x-dotwebstack-";

  public static final String INFORMATION_PRODUCT = DOTWEBSTACK_PREFIX.concat("information-product");

  public static final String PARAMETER = DOTWEBSTACK_PREFIX.concat("parameter");

  public static final String LDPATH = DOTWEBSTACK_PREFIX.concat("ldpath");

  public static final String LDPATH_NAMESPACES = DOTWEBSTACK_PREFIX.concat("ldpath-namespaces");

  public static final String TYPE = DOTWEBSTACK_PREFIX.concat("type");

  public static final String CONSTANT_VALUE = "x-constant-value";

  public static final String SUBJECT_FILTER = DOTWEBSTACK_PREFIX.concat("subject-filter");

  public static final String SUBJECT_FILTER_PREDICATE = "predicate";

  public static final String SUBJECT_FILTER_OBJECT = "object";

  public static final String RELATIVE_LINK = "x-relative-link";

  public static final String EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL =
      DOTWEBSTACK_PREFIX.concat("exclude-properties-when-empty-or-null");

  private OpenApiSpecificationExtensions() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", OpenApiSpecificationExtensions.class));
  }

}
