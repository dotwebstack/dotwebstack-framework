package org.dotwebstack.framework.frontend.soap.wsdlreader;

/**
 * Default exception thrown by the SoapBuilder.
 *
 * @author Tom Bujok
 * @since 1.0.0
 */
public class SoapBuilderException extends SoapException {
  public SoapBuilderException(String message) {
    super(message);
  }

  public SoapBuilderException(String message, Throwable cause) {
    super(message, cause);
  }

  public SoapBuilderException(Throwable cause) {
    super(cause.getMessage(), cause);
  }
}
