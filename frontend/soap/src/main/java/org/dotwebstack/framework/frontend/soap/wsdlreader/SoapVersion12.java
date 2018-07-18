package org.dotwebstack.framework.frontend.soap.wsdlreader;

import javax.xml.namespace.QName;

class SoapVersion12 implements SoapVersion {

  private static final QName envelopeQName = new QName(Constants.SOAP12_ENVELOPE_NS, "Envelope");
  private static final QName bodyQName = new QName(Constants.SOAP12_ENVELOPE_NS, "Body");
  private static final QName faultQName = new QName(Constants.SOAP11_ENVELOPE_NS, "Fault");
  private static final QName headerQName = new QName(Constants.SOAP12_ENVELOPE_NS, "Header");
  public static final SoapVersion12 INSTANCE = new SoapVersion12();

  private SoapVersion12() {
  }

  public String getEncodingNamespace() {
    return "http://www.w3.org/2003/05/soap-encoding";
  }

  public String getEnvelopeNamespace() {
    return Constants.SOAP12_ENVELOPE_NS;
  }

  public String toString() {
    return "SOAP 1.2";
  }

  public QName getBodyQName() {
    return bodyQName;
  }

  public QName getEnvelopeQName() {
    return envelopeQName;
  }

  public QName getHeaderQName() {
    return headerQName;
  }

  public static QName getFaultQName() {
    return faultQName;
  }

  public String getName() {
    return "SOAP 1.2";
  }

  public String getFaultDetailNamespace() {
    return getEnvelopeNamespace();
  }
}
