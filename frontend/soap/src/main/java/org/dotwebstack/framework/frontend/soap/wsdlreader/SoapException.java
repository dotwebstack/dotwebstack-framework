package org.dotwebstack.framework.frontend.soap.wsdlreader;

/**
 * Top-level exception type thrown by soap-ws
 *
 * @author Tom Bujok
 * @since 1.0.0
 */
public class SoapException extends RuntimeException {
  public SoapException(String s) {
    super(s);
  }

  public SoapException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public SoapException(Throwable throwable) {
    super(throwable.getMessage(), throwable);
  }
}
