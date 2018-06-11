package org.dotwebstack.framework.frontend.soap.wsdlreader;

import javax.xml.namespace.QName;

interface SoapVersion {
  public static final SoapVersion11 Soap11 = SoapVersion11.instance;
  public static final SoapVersion12 Soap12 = SoapVersion12.instance;

  public QName getEnvelopeQName();

  public QName getBodyQName();

  public QName getHeaderQName();

  public String getEnvelopeNamespace();

  public String getFaultDetailNamespace();

  public String getEncodingNamespace();
  
  public String getName();

}
