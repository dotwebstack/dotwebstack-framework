package org.dotwebstack.framework.frontend.soap.wsdlreader;

/**
 * This class was extracted from the soapUI code base by centeractive ag in October 2011.
 * The main reason behind the extraction was to separate the code that is responsible
 * for the generation of the SOAP messages from the rest of the soapUI's code that is
 * tightly coupled with other modules, such as soapUI's graphical user interface, etc.
 * The goal was to create an open-source java project whose main responsibility is to
 * handle SOAP message generation and SOAP transmission purely on an XML level.
 * <br/>
 * centeractive ag would like to express strong appreciation to SmartBear Software and
 * to the whole team of soapUI's developers for creating soapUI and for releasing its
 * source code under a free and open-source licence. centeractive ag extracted and
 * modifies some parts of the soapUI's code in good faith, making every effort not
 * to impair any existing functionality and to supplement it according to our
 * requirements, applying best practices of software design.
 *
 * <p>Changes done:
 * - changing location in the package structure
 * - removal of dependencies and code parts that are out of scope of SOAP message generation
 * - minor fixes to make the class compile out of soapUI's code base
 */

/**
 * Namespace Constants
 *
 * @author ole.matzura
 */

public class Constants {
  private Constants() {
    throw new IllegalStateException("Utility class Constants");
  }

  public static final String ERROR_RESPONSE = "<?xml version=\"1.0\"?>"
      + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "  <s:Body>"
      + "    <s:Fault>"
      + "      <faultcode xmlns:a=\"http://schemas.microsoft.com/ws/2005/05/addressing/none\">"
      + "a:ActionNotSupported</faultcode>"
      + "      <faultstring xml:lang=\"en-US\">The message with Action '%s' cannot be processed "
      + "at the receiver, due to a ContractFilter mismatch at the EndpointDispatcher. This may "
      + "be because of either a contract mismatch (mismatched Actions between sender and "
      + "receiver) or a binding/security mismatch between the sender and the receiver.  Check "
      + "that sender and receiver have the same contract and the same binding (including "
      + "security requirements, e.g. Message, Transport, None).</faultstring>" + "    </s:Fault>"
      + "  </s:Body>" + "</s:Envelope>";
  public static final String DEFAULT_UUID = "uuid:e2347d89-ea40-45fd-802f-5fcc266a3858+id=1";

  public static final String DWS_NS = "http://dotwebstack.org/wsdl-extension/";


  static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
  static final String XML_NS_PREFIX = "xmlns";
  static final String XML_NS = "http://www.w3.org/2000/xmlns/";
  static final String SOAP12_HTTP_BINDING_NS = "http://www.w3.org/2003/05/soap/bindings/HTTP/";
  static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
  static final String XSI_NS_2000 = "http://www.w3.org/2000/XMLSchema-instance";
  static final String XMLMIME_NS = "http://www.w3.org/2005/05/xmlmime";

  static final String SOAP12_ENVELOPE_NS = "http://www.w3.org/2003/05/soap-envelope";
  static final String SOAP11_ENVELOPE_NS = "http://schemas.xmlsoap.org/soap/envelope/";

  static final String WSDL11_NS = "http://schemas.xmlsoap.org/wsdl/";
  static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";
  static final String SOAP_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
  static final String SOAP_HTTP_BINDING_NS = "http://schemas.xmlsoap.org/wsdl/soap/";

  static final String WADL10_NS = "http://research.sun.com/wadl/2006/10";
  static final String WADL11_NS = "http://wadl.dev.java.net/2009/02";
  static final String SOAP_MICROSOFT_TCP = "http://schemas.microsoft.com/wse/2003/06/tcp";


}
