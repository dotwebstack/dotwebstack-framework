package org.dotwebstack.framework.frontend.soap.wsdlreader;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Specifies the context of the SOAP message generation.
 *
 * @author Tom Bujok
 * @since 1.0.0
 */
public class SoapContext {

  public static final SoapContext DEFAULT = SoapContext.builder().build();
  public static final SoapContext NO_CONTENT = SoapContext.builder().exampleContent(false).build();

  /**
   * Generates comments with type information in new requests
   */
  private final boolean typeComments;
  private final boolean valueComments;
  private final boolean exampleContent;
  private final boolean buildOptional;
  private final boolean alwaysBuildHeaders;

  /*
   * A list of XML-Schema types and global elements in the form of name@namespace which
   * will be excluded when generating sample requests and responses and input forms.
   * By default the XML-Schema root element is added since it is quite common in .NET
   * services and generates a sample xml fragment of about 300 kb!.
   */
  private final Set<QName> excludedTypes;
  private final SoapMultiValuesProvider multiValuesProvider;

  /**
   * Constructor mainly for SpringFramework purposes, in any other case use the fluent builder interface;
   * #see builder() method
   *
   * @param exampleContent exampleContent
   * @param typeComments typeComments
   * @param valueComments valueComments
   * @param buildOptional buildOptional
   * @param alwaysBuildHeaders alwaysBuildHeaders
   * @param excludedTypes excludedTypes
   */
  public SoapContext(boolean exampleContent, boolean typeComments, boolean valueComments,
             boolean buildOptional, boolean alwaysBuildHeaders,
             Set<QName> excludedTypes, SoapMultiValuesProvider multiValuesProvider) {
    this.exampleContent = exampleContent;
    this.typeComments = typeComments;
    this.valueComments = valueComments;
    this.buildOptional = buildOptional;
    this.alwaysBuildHeaders = alwaysBuildHeaders;
    this.excludedTypes = new HashSet<QName>(excludedTypes);
    this.multiValuesProvider = multiValuesProvider;
  }

  /**
   * Constructor mainly for SpringFramework purposes, in any other case use the fluent builder interface;
   * #see builder() method
   *
   * @param exampleContent exampleContent
   * @param typeComments typeComments
   * @param valueComments valueComments
   * @param buildOptional buildOptional
   * @param alwaysBuildHeaders alwaysBuildHeaders
   */
  public SoapContext(boolean exampleContent, boolean typeComments, boolean valueComments,
             boolean buildOptional, boolean alwaysBuildHeaders) {
    this.exampleContent = exampleContent;
    this.typeComments = typeComments;
    this.valueComments = valueComments;
    this.buildOptional = buildOptional;
    this.alwaysBuildHeaders = alwaysBuildHeaders;
    this.excludedTypes = new HashSet<QName>();
    this.multiValuesProvider = null;
  }

  public boolean isBuildOptional() {
    return buildOptional;
  }

  public boolean isAlwaysBuildHeaders() {
    return alwaysBuildHeaders;
  }

  public boolean isExampleContent() {
    return exampleContent;
  }

  public boolean isTypeComments() {
    return typeComments;
  }

  public boolean isValueComments() {
    return valueComments;
  }

  public Set<QName> getExcludedTypes() {
    return new HashSet<QName>(excludedTypes);
  }

  public SoapMultiValuesProvider getMultiValuesProvider() {
    return multiValuesProvider;
  }

  public static ContextBuilder builder() {
    return new ContextBuilder();
  }

  public static class ContextBuilder {
    private boolean exampleContent = true;
    private boolean typeComments = false;
    private boolean valueComments = false;
    private boolean buildOptional = true;
    private boolean alwaysBuildHeaders = true;
    private Set<QName> excludedTypes = new HashSet<QName>();
    private SoapMultiValuesProvider multiValuesProvider = null;

    /**
     * Specifies if to generate example SOAP message content
     *
     * @param value value
     * @return builder
     */
    public ContextBuilder exampleContent(boolean value) {
      this.exampleContent = value;
      return this;
    }

    /**
     * Specifies if to generate SOAP message type comments
     *
     * @param value value
     * @return builder
     */
    public ContextBuilder typeComments(boolean value) {
      this.typeComments = value;
      return this;
    }

    /**
     * Specifies if to skip SOAP message comments
     *
     * @param value value
     * @return builder
     */
    public ContextBuilder valueComments(boolean value) {
      this.valueComments = value;
      return this;
    }

    /**
     * Specifies if to generate content for elements marked as optional
     *
     * @param value value
     * @return builder
     */
    public ContextBuilder buildOptional(boolean value) {
      this.buildOptional = value;
      return this;
    }

    /**
     * Specifies if to always build SOAP headers
     *
     * @param value value
     * @return builder
     */
    public ContextBuilder alwaysBuildHeaders(boolean value) {
      this.alwaysBuildHeaders = value;
      return this;
    }

    /**
     * A list of XML-Schema types and global elements in the form of name@namespace which
     * will be excluded when generating sample requests and responses and input forms.
     * By default the XML-Schema root element is added since it is quite common in .NET
     * services and generates a sample xml fragment of about 300 kb!.
     *
     * @param excludedTypes excludedTypes
     * @return builder
     */
    public ContextBuilder excludedTypes(Set<QName> excludedTypes) {
      this.excludedTypes = new HashSet<QName>(excludedTypes);
      return this;
    }

    public ContextBuilder multiValuesProvider(SoapMultiValuesProvider multiValuesProvider) {
      this.multiValuesProvider = multiValuesProvider;
      return this;
    }

    /**
     * Builds populated context instance
     *
     * @return fully populated soap context
     */
    public SoapContext build() {
      return new SoapContext(exampleContent, typeComments, valueComments,
          buildOptional, alwaysBuildHeaders, excludedTypes, multiValuesProvider);
    }
  }

}
