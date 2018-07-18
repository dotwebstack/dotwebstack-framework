package org.dotwebstack.framework.frontend.soap.wsdlreader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Wsdl-related tools
 *
 * @author Ole.Matzura
 */
class WsdlUtils {
  private static final Logger LOG = LoggerFactory.getLogger(WsdlUtils.class);

  public static <T extends ExtensibilityElement> T getExtensiblityElement(
      List<?> list,
      Class<T> clazz) {
    List<T> elements = getExtensiblityElements(list, clazz);
    return elements.isEmpty() ? null : elements.get(0);
  }

  public static <T extends ExtensibilityElement> List<T> getExtensiblityElements(
      List list,
      Class<T> clazz) {
    List<T> result = new ArrayList<>();

    for (Iterator<T> i = list.iterator(); i.hasNext(); ) {
      T elm = i.next();
      if (clazz.isAssignableFrom(elm.getClass())) {
        result.add(elm);
      }
    }

    return result;
  }

  @SuppressWarnings("squid:S3776")
  public static Binding findBindingForOperation(
      Definition definition,
      BindingOperation bindingOperation) {
    Map services = definition.getAllServices();
    Iterator<Service> s = services.values().iterator();

    while (s.hasNext()) {
      Map ports = s.next().getPorts();
      Iterator<Port> p = ports.values().iterator();
      while (p.hasNext()) {
        Binding binding = p.next().getBinding();
        List bindingOperations = binding.getBindingOperations();
        for (Iterator iter = bindingOperations.iterator(); iter.hasNext(); ) {
          BindingOperation op = (BindingOperation) iter.next();
          if (op.getName().equals(bindingOperation.getName())) {
            return binding;
          }
        }
      }
    }

    Map bindings = definition.getAllBindings();
    Iterator<QName> names = bindings.keySet().iterator();
    while (names.hasNext()) {
      Binding binding = definition.getBinding(names.next());
      List bindingOperations = binding.getBindingOperations();
      for (Iterator iter = bindingOperations.iterator(); iter.hasNext(); ) {
        BindingOperation op = (BindingOperation) iter.next();
        if (op.getName().equals(bindingOperation.getName())) {
          return binding;
        }
      }
    }

    return null;
  }

  public static boolean isInputSoapEncoded(BindingOperation bindingOperation) {
    if (bindingOperation == null) {
      return false;
    }

    BindingInput bindingInput = bindingOperation.getBindingInput();
    if (bindingInput == null) {
      return false;
    }

    SOAPBody soapBody =
        WsdlUtils.getExtensiblityElement(bindingInput.getExtensibilityElements(), SOAPBody.class);

    if (soapBody != null) {
      return soapBody.getUse() != null
          && soapBody.getUse().equalsIgnoreCase("encoded")
          && (soapBody.getEncodingStyles() == null || soapBody.getEncodingStyles().contains(
          "http://schemas.xmlsoap.org/soap/encoding/"));
    }

    SOAP12Body soap12Body =
        WsdlUtils.getExtensiblityElement(bindingInput.getExtensibilityElements(),
        SOAP12Body.class);

    if (soap12Body != null) {
      return soap12Body.getUse() != null
          && soap12Body.getUse().equalsIgnoreCase("encoded")
          && (soap12Body.getEncodingStyle() == null || soap12Body.getEncodingStyle().equals(
          "http://www.w3.org/2001/12/soap-encoding"));
    }

    return false;
  }

  public static boolean isRpc(Definition definition, BindingOperation bindingOperation) {
    SOAPOperation soapOperation =
        WsdlUtils.getExtensiblityElement(bindingOperation.getExtensibilityElements(),
        SOAPOperation.class);

    if (soapOperation != null && soapOperation.getStyle() != null) {
      return soapOperation.getStyle().equalsIgnoreCase("rpc");
    }

    SOAP12Operation soap12Operation =
        WsdlUtils.getExtensiblityElement(bindingOperation.getExtensibilityElements(),
        SOAP12Operation.class);

    if (soap12Operation != null && soap12Operation.getStyle() != null) {
      return soap12Operation.getStyle().equalsIgnoreCase("rpc");
    }

    Binding binding = findBindingForOperation(definition, bindingOperation);
    if (binding == null) {
      LOG.warn("Failed to find binding for operation [" + bindingOperation.getName()
          + "] in definition [" + definition.getDocumentBaseURI() + "]");
      return false;
    }

    return isRpc(binding);
  }

  public static boolean isRpc(Binding binding) {
    SOAPBinding soapBinding = WsdlUtils
        .getExtensiblityElement(binding.getExtensibilityElements(), SOAPBinding.class);

    if (soapBinding != null) {
      return "rpc".equalsIgnoreCase(soapBinding.getStyle());
    }

    SOAP12Binding soap12Binding =
        WsdlUtils.getExtensiblityElement(binding.getExtensibilityElements(), SOAP12Binding.class);

    if (soap12Binding != null) {
      return "rpc".equalsIgnoreCase(soap12Binding.getStyle());
    }

    return false;
  }

  public static boolean isAttachmentOutputPart(Part part, BindingOperation operation) {
    return getOutputMultipartContent(part, operation).length > 0;
  }

  public static MIMEContent[] getOutputMultipartContent(Part part, BindingOperation operation) {
    BindingOutput output = operation.getBindingOutput();
    if (output == null) {
      return new MIMEContent[0];
    }

    MIMEMultipartRelated multipartOutput =
        WsdlUtils.getExtensiblityElement(output.getExtensibilityElements(),
        MIMEMultipartRelated.class);

    return getContentParts(part, multipartOutput);
  }

  public static MIMEContent[] getContentParts(Part part, MIMEMultipartRelated multipart) {
    List<MIMEContent> result = new ArrayList<>();

    if (multipart != null) {
      List<MIMEPart> parts = multipart.getMIMEParts();

      for (int c = 0; c < parts.size(); c++) {
        List<MIMEContent> contentParts = WsdlUtils.getExtensiblityElements(parts.get(c)
            .getExtensibilityElements(), MIMEContent.class);

        for (MIMEContent content : contentParts) {
          if (content.getPart().equals(part.getName())) {
            result.add(content);
          }
        }
      }
    }

    return result.toArray(new MIMEContent[result.size()]);
  }

  public static Part[] getOutputParts(BindingOperation operation) {
    BindingOutput bindingOutput = operation.getBindingOutput();
    if (bindingOutput == null) {
      return new Part[0];
    }

    List<Part> result = new ArrayList<>();
    Output output = operation.getOperation().getOutput();
    if (output == null) {
      return new Part[0];
    }

    Message msg = output.getMessage();
    if (msg != null) {
      SOAPBody soapBody = WsdlUtils
          .getExtensiblityElement(bindingOutput.getExtensibilityElements(), SOAPBody.class);

      if (soapBody == null || soapBody.getParts() == null) {
        SOAP12Body soap12Body =
            WsdlUtils.getExtensiblityElement(bindingOutput.getExtensibilityElements(),
            SOAP12Body.class);

        if (soap12Body == null || soap12Body.getParts() == null) {
          result.addAll(msg.getOrderedParts(null));
        } else {
          result = addParts(soap12Body.getParts(), msg);
        }
      } else {
        result = addParts(soapBody.getParts(), msg);
      }
    } else {
      LOG.warn("Missing output message for binding operation [{}]", operation.getName());
    }

    return result.toArray(new Part[result.size()]);
  }

  // Add the parts in a message to a list of parts.
  public static List<Part> addParts(List<String> parts, Message msg) {
    List<Part> result = new ArrayList<>();
    Iterator i = parts.iterator();
    while (i.hasNext()) {
      String partName = (String) i.next();
      Part part = msg.getPart(partName);
      result.add(part);
    }

    return result;
  }

  public static String getSoapBodyNamespace(List<?> list) {
    SOAPBody soapBody = WsdlUtils.getExtensiblityElement(list, SOAPBody.class);
    if (soapBody != null) {
      return soapBody.getNamespaceURI();
    }

    SOAP12Body soap12Body = WsdlUtils.getExtensiblityElement(list, SOAP12Body.class);
    if (soap12Body != null) {
      return soap12Body.getNamespaceURI();
    }

    return null;
  }

  /**
   * A SOAP-Header wrapper
   *
   * @author ole.matzura
   */

  public interface SoapHeader {
    public QName getMessage();

    public String getPart();
  }

  /**
   * SOAP 1.1 Header implementation
   *
   * @author ole.matzura
   */

  public static class Soap11Header implements SoapHeader {
    private final SOAPHeader soapHeader;

    public Soap11Header(SOAPHeader soapHeader) {
      this.soapHeader = soapHeader;
    }

    public QName getMessage() {
      return soapHeader.getMessage();
    }

    public String getPart() {
      return soapHeader.getPart();
    }
  }

  /**
   * SOAP 1.2 Header implementation
   *
   * @author ole.matzura
   */

  public static class Soap12Header implements SoapHeader {
    private final SOAP12Header soapHeader;

    public Soap12Header(SOAP12Header soapHeader) {
      this.soapHeader = soapHeader;
    }

    public QName getMessage() {
      return soapHeader.getMessage();
    }

    public String getPart() {
      return soapHeader.getPart();
    }
  }

  public static List<SoapHeader> getSoapHeaders(List list) {
    List<SoapHeader> result = new ArrayList<>();

    List<SOAPHeader> soapHeaders = WsdlUtils.getExtensiblityElements(list, SOAPHeader.class);
    if (!soapHeaders.isEmpty()) {
      for (SOAPHeader header : soapHeaders) {
        result.add(new Soap11Header(header));
      }
    } else {
      List<SOAP12Header> soap12Headers =
          WsdlUtils.getExtensiblityElements(list, SOAP12Header.class);
      if (!soap12Headers.isEmpty()) {
        for (SOAP12Header header : soap12Headers) {
          result.add(new Soap12Header(header));
        }
      }
    }

    return result;
  }

  public static String getTargetNamespace(Definition definition) {
    return definition.getTargetNamespace() == null
        ? XMLConstants.NULL_NS_URI
        : definition.getTargetNamespace();
  }
}
