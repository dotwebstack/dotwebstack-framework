package org.dotwebstack.framework.frontend.soap.wsdlreader;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class WsdlNamespaceContext implements NamespaceContext {
  @SuppressWarnings("AbbreviationAsWordInName")
  public String getNamespaceURI(String prefix) {
    if (prefix == null) {
      throw new NullPointerException("Null prefix");
    }
    switch (prefix) {
      case "xs": return Constants.XSD_NS;
      case "dws": return Constants.DWS_NS;
      case "xml": return XMLConstants.XML_NS_URI;
      case "soapenv": return Constants.SOAP11_ENVELOPE_NS;
      default: return XMLConstants.NULL_NS_URI;
    }
  }

  // This method isn't necessary for XPath processing, but it occurs
  // in the interface NamespaceContext.
  public String getPrefix(String uri) {
    throw new UnsupportedOperationException();
  }

  // This method isn't necessary for XPath processing either.
  public Iterator getPrefixes(String uri) {
    throw new UnsupportedOperationException();
  }

}
