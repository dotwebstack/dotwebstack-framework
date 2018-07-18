package org.dotwebstack.framework.frontend.soap.wsdlreader;

import javax.xml.namespace.QName;

interface SoapVersion {
  public QName getEnvelopeQName();

  public QName getBodyQName();

  public QName getHeaderQName();

  public String getEnvelopeNamespace();

  public String getFaultDetailNamespace();

  public String getEncodingNamespace();

  public String getName();

}
