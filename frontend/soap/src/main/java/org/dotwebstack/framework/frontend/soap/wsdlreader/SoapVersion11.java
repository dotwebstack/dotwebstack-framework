package org.dotwebstack.framework.frontend.soap.wsdlreader;

import javax.xml.namespace.QName;

class SoapVersion11 implements SoapVersion {
  private static final QName envelopeQName = new QName(Constants.SOAP11_ENVELOPE_NS, "Envelope");
  private static final QName bodyQName = new QName(Constants.SOAP11_ENVELOPE_NS, "Body");
  private static final QName faultQName = new QName(Constants.SOAP11_ENVELOPE_NS, "Fault");
  private static final QName headerQName = new QName(Constants.SOAP11_ENVELOPE_NS, "Header");

  public static final SoapVersion11 instance = new SoapVersion11();

  private SoapVersion11() {
  }

  public String getEnvelopeNamespace() {
    return Constants.SOAP11_ENVELOPE_NS;
  }

  public String getEncodingNamespace() {
    return Constants.SOAP_ENCODING_NS;
  }

  public String toString() {
    return "SOAP 1.1";
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

  public String getName() {
    return "SOAP 1.1";
  }

  public String getFaultDetailNamespace() {
    return "";
  }
}
