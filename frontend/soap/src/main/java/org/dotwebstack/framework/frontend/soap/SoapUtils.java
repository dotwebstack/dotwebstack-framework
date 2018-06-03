package org.dotwebstack.framework.frontend.soap;

import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl;
import javax.wsdl.extensions.ExtensibilityElement;
/*
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Types;
import javax.wsdl.Service;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
*/

public class SoapUtils {

  public static String getLocationUri(ExtensibilityElement exElement) {
    if (exElement instanceof SOAP12AddressImpl) {
      return ((SOAP12AddressImpl) exElement).getLocationURI();
    } else if (exElement instanceof SOAPAddressImpl) {
      return ((SOAPAddressImpl) exElement).getLocationURI();
    } else if (exElement instanceof HTTPAddressImpl) {
      return ((HTTPAddressImpl) exElement).getLocationURI();
    } else {
      return "UNSUPPORTED";
    }
  }

}