package org.dotwebstack.framework.frontend.openapi;

public final class OpenApiSpecificationExtensions {

  public static final String DOTWEBSTACK_PREFIX = "x-dotwebstack-";

  public static final String INFORMATION_PRODUCT = DOTWEBSTACK_PREFIX.concat("information-product");

  public static final String PARAMETER = DOTWEBSTACK_PREFIX.concat("parameter");

  public static final String LDPATH = DOTWEBSTACK_PREFIX.concat("ldpath");

  public static final String LDPATH_NAMESPACES = DOTWEBSTACK_PREFIX.concat("ldpath-namespaces");

  public static final String TYPE = DOTWEBSTACK_PREFIX.concat("type");

  public static final String CONSTANT_VALUE = DOTWEBSTACK_PREFIX.concat("constant-value");

  public static final String SUBJECT_QUERY = DOTWEBSTACK_PREFIX.concat("subject-query");

  public static final String RELATIVE_LINK = DOTWEBSTACK_PREFIX.concat("relative-link");

  public static final String EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL =
      DOTWEBSTACK_PREFIX.concat("exclude-properties-when-empty-or-null");

  /**
   * Use this vendor extension to configure when an endpoint should give a HTTP 404 response. The
   * value of this vendor extension should configure a SPARQL ASK query which will be applied to the
   * resulting linked data from the back end. If the ASK query returns false, a 404 response is
   * returned. If the query returns true, the default flow is continued.
   * <ul>
   * <li>The vendor extension should be applied on the operation level;</li>
   * <li>The extension should be accompanied by a 404 response (and vice versa);</li>
   * <li>See the dotwebstack-integration-test/.../OpenApiIntegrationTest for an usage example.</li>
   * </ul>
   */
  public static final String RESULT_FOUND_QUERY = DOTWEBSTACK_PREFIX.concat("result-found-query");

  private OpenApiSpecificationExtensions() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", OpenApiSpecificationExtensions.class));
  }

}
