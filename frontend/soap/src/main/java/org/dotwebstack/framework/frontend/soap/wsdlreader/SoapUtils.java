package org.dotwebstack.framework.frontend.soap.wsdlreader;

import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;

public class SoapUtils {

  /* *********************
   *
   *
   * Main public methods
   *
   *
   ********************* */
  public static String getLocationURI(ExtensibilityElement exElement) {
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

  public static void printSchema(Definition definition, Schema schema) {
    
    Element schemaElement = schema.getElement(); 
    Map<String, String> namespaces = definition.getNamespaces(); 
    for (Entry<String, String> entry : namespaces.entrySet()) { 
      if (entry.getKey().equals("xmlns") || entry.getKey().trim().isEmpty()) { 
        continue; 
      } 
      if (schemaElement.getAttribute("xmlns:" + entry.getKey()).isEmpty()) { 
        schemaElement.setAttribute("xmlns:" + entry.getKey(), entry.getValue()); 
      } 
    }
    printElement(schemaElement);
  }

  public static void printElement(Element element) {
    
    try {
      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();
      StringWriter buffer = new StringWriter();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(new DOMSource(element), new StreamResult(buffer));
      System.out.println(buffer.toString());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
  
  /* *********************
   *
   *
   * From Reficio / SoapUI
   *
   *
   ********************* */
  private static void addHeaders(SchemaDefinitionWrapper definitionWrapper, List<WsdlUtils.SoapHeader> headers, SoapVersion soapVersion, XmlCursor cursor, SampleXmlUtil xmlGenerator) throws Exception {
    // reposition
    cursor.toStartDoc();
    cursor.toChild(soapVersion.getEnvelopeQName());
    cursor.toFirstChild();

    cursor.beginElement(soapVersion.getHeaderQName());
    cursor.toFirstChild();

    for (int i = 0; i < headers.size(); i++) {
      WsdlUtils.SoapHeader header = headers.get(i);

      Message message = definitionWrapper.getDefinition().getMessage(header.getMessage());
      if (message == null) {
        System.out.println("Missing message for header: " + header.getMessage());
        continue;
      }

      Part part = message.getPart(header.getPart());

      if (part != null) {
        createElementForPart(definitionWrapper, part, cursor, xmlGenerator);
      }
      else {
        System.out.println("Missing part for header; " + header.getPart());
      }
    }
  }

  private static void createElementForPart(SchemaDefinitionWrapper definitionWrapper, Part part, XmlCursor cursor, SampleXmlUtil xmlGenerator) throws Exception {
    QName elementName = part.getElementName();
    QName typeName = part.getTypeName();

    if (elementName != null) {
      cursor.beginElement(elementName);

      if (definitionWrapper.hasSchemaTypes()) {
        SchemaGlobalElement elm = definitionWrapper.getSchemaTypeLoader().findElement(elementName);
        if (elm != null) {
          cursor.toFirstChild();
          xmlGenerator.createSampleForType(elm.getAnnotation(), elm.getType(), cursor);
        } else {
          System.out.println("Could not find element [" + elementName + "] specified in part [" + part.getName() + "]");
        }
      }

      cursor.toParent();
    } else {
      // cursor.beginElement( new QName(
      // wsdlContext.getWsdlDefinition().getTargetNamespace(), part.getName()
      // ));
      cursor.beginElement(part.getName());
      if (typeName != null && definitionWrapper.hasSchemaTypes()) {
        SchemaType type = definitionWrapper.getSchemaTypeLoader().findType(typeName);

        if (type != null) {
          cursor.toFirstChild();
          xmlGenerator.createSampleForType(null, type, cursor);
        } else {
          System.out.println("Could not find type [" + typeName + "] specified in part [" + part.getName() + "]");
        }
      }

      cursor.toParent();
    }
  }

  private static SoapVersion getSoapVersion(Binding binding) {
    List<?> list = binding.getExtensibilityElements();

    SOAPBinding soapBinding = WsdlUtils.getExtensiblityElement(list, SOAPBinding.class);
    if (soapBinding != null) {
      if ((soapBinding.getTransportURI().startsWith(Constants.SOAP_HTTP_TRANSPORT) || soapBinding
          .getTransportURI().startsWith(Constants.SOAP_MICROSOFT_TCP))) {
        return SoapVersion.Soap11;
      }
    }

    SOAP12Binding soap12Binding = WsdlUtils.getExtensiblityElement(list, SOAP12Binding.class);
    if (soap12Binding != null) {
      if (soap12Binding.getTransportURI().startsWith(Constants.SOAP_HTTP_TRANSPORT)
          || soap12Binding.getTransportURI().startsWith(Constants.SOAP12_HTTP_BINDING_NS)
          || soap12Binding.getTransportURI().startsWith(Constants.SOAP_MICROSOFT_TCP)) {
        return SoapVersion.Soap12;
      }
    }
    throw new SoapBuilderException("SOAP binding not recognized");
  }

  public static String buildSoapMessageFromOutput(SchemaDefinitionWrapper definitionWrapper, Binding binding, BindingOperation bindingOperation, SoapContext context) throws Exception {
    boolean inputSoapEncoded = WsdlUtils.isInputSoapEncoded(bindingOperation);
    SampleXmlUtil xmlGenerator = new SampleXmlUtil(inputSoapEncoded, context);
    SoapVersion soapVersion = getSoapVersion(binding);


    XmlObject object = XmlObject.Factory.newInstance();
    XmlCursor cursor = object.newCursor();
    cursor.toNextToken();
    cursor.beginElement(soapVersion.getEnvelopeQName());

    if (inputSoapEncoded) {
      cursor.insertNamespace("xsi", Constants.XSI_NS);
      cursor.insertNamespace("xsd", Constants.XSD_NS);
    }

    cursor.toFirstChild();

    cursor.beginElement(soapVersion.getBodyQName());
    cursor.toFirstChild();

    if (WsdlUtils.isRpc(definitionWrapper.getDefinition(), bindingOperation)) {
      buildRpcResponse(definitionWrapper, bindingOperation, soapVersion, cursor, xmlGenerator);
    } else {
      buildDocumentResponse(definitionWrapper, bindingOperation, cursor, xmlGenerator);
    }

    if (context.isAlwaysBuildHeaders()) {
      // bindingOutput will be null for one way operations,
      // but then we shouldn't be here in the first place???
      BindingOutput bindingOutput = bindingOperation.getBindingOutput();
      if (bindingOutput != null) {
        List<?> extensibilityElements = bindingOutput.getExtensibilityElements();
        List<WsdlUtils.SoapHeader> soapHeaders = WsdlUtils.getSoapHeaders(extensibilityElements);
        addHeaders(definitionWrapper, soapHeaders, soapVersion, cursor, xmlGenerator);
      }
    }
    cursor.dispose();

    try {
      StringWriter writer = new StringWriter();
      XmlUtils.serializePretty(object, writer);
      return writer.toString();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return object.xmlText();
    }
  }

  private static void buildDocumentResponse(SchemaDefinitionWrapper definitionWrapper, BindingOperation bindingOperation, XmlCursor cursor, SampleXmlUtil xmlGenerator)
      throws Exception {
    Part[] parts = WsdlUtils.getOutputParts(bindingOperation);

    for (int i = 0; i < parts.length; i++) {
      Part part = parts[i];

      if (!WsdlUtils.isAttachmentOutputPart(part, bindingOperation)
          && (part.getElementName() != null || part.getTypeName() != null)) {
        XmlCursor c = cursor.newCursor();
        c.toLastChild();
        createElementForPart(definitionWrapper, part, c, xmlGenerator);
        c.dispose();
      }
    }
  }

  private static void buildRpcResponse(SchemaDefinitionWrapper definitionWrapper, BindingOperation bindingOperation, SoapVersion soapVersion, XmlCursor cursor, SampleXmlUtil xmlGenerator)
      throws Exception {
    // rpc requests use the operation name as root element
    BindingOutput bindingOutput = bindingOperation.getBindingOutput();
    String ns = bindingOutput == null ? null : WsdlUtils.getSoapBodyNamespace(bindingOutput
        .getExtensibilityElements());

    if (ns == null) {
      ns = WsdlUtils.getTargetNamespace(definitionWrapper.getDefinition());
      System.out.println("missing namespace on soapbind:body for RPC response, using targetNamespace instead (BP violation)");
    }

    cursor.beginElement(new QName(ns, bindingOperation.getName() + "Response"));
    if (xmlGenerator.isSoapEnc()) {
      cursor.insertAttributeWithValue(new QName(soapVersion.getEnvelopeNamespace(),
          "encodingStyle"), soapVersion.getEncodingNamespace());
    }

    Part[] inputParts = WsdlUtils.getOutputParts(bindingOperation);
    for (int i = 0; i < inputParts.length; i++) {
      Part part = inputParts[i];
      if (WsdlUtils.isAttachmentOutputPart(part, bindingOperation)) {
        // if( iface.getSettings().getBoolean( WsdlSettings.ATTACHMENT_PARTS ) )
        {
          XmlCursor c = cursor.newCursor();
          c.toLastChild();
          c.beginElement(part.getName());
          c.insertAttributeWithValue("href", part.getName() + "Attachment");
          c.dispose();
        }
      } else {
        if (definitionWrapper.hasSchemaTypes()) {
          QName typeName = part.getTypeName();
          if (typeName != null) {
            SchemaType type = definitionWrapper.findType(typeName);

            if (type != null) {
              XmlCursor c = cursor.newCursor();
              c.toLastChild();
              c.insertElement(part.getName());
              c.toPrevToken();

              xmlGenerator.createSampleForType(null, type, c);
              c.dispose();
            } else {
              System.out.println("Failed to find type [" + typeName + "]");
            }
          } else {
            SchemaGlobalElement element = definitionWrapper.getSchemaTypeLoader().findElement(part.getElementName());
            if (element != null) {
              XmlCursor c = cursor.newCursor();
              c.toLastChild();
              c.insertElement(element.getName());
              c.toPrevToken();

              xmlGenerator.createSampleForType(element.getAnnotation(), element.getType(), c);
              c.dispose();
            } else {
              System.out.println("Failed to find element [" + part.getElementName() + "]");
            }
          }
        }
      }
    }
  }
}
