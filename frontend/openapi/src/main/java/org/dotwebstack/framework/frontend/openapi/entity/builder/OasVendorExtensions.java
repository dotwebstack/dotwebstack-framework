package org.dotwebstack.framework.frontend.openapi.entity.builder;


public final class OasVendorExtensions {

  public static final String EXCLUDE_EMPTY_AND_NULL_VALUE_PROPERTIES =
      "x-exclude-empty-and-null-value-properties";

  public static final String LDPATH = "x-ldpath";
  public static final String KEY_LDPATH = "x-key-ldpath";

  public static final String LDPATH_NAMESPACES = "x-ldpath-namespaces";

  public static final String PRODUCT = "x-product";

  public static final String RELATIVE_LINK = "x-relative-link";

  public static final String CONTEXT_LINK = "x-context-links";

  public static final String RESULT_REF = "x-result-ref";

  public static final String TYPE = "x-type";

  public static final String CONSTANT_VALUE = "x-constant-value";

  /* can optionally be used for collection endpoint; defines maximum number of documents per page */
  static final String COLLECTION_PAGE_SIZE = "x-page-size";

  /*
   * is used to enable pagination for collection endpoint (the one which can return multiple
   * documents)
   */
  public static final String X_PAGINATION = "x-pagination";

  private OasVendorExtensions() {

  }

}
