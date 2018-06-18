package org.dotwebstack.framework.frontend.soap.handlers;

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import lombok.NonNull;

import org.dotwebstack.framework.frontend.soap.wsdlreader.SchemaDefinitionWrapper;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapContext;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapUtils;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class SoapRequestHandler implements Inflector<ContainerRequestContext, String> {

  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestHandler.class);

  private static final String DWS_NAMESPACE = "http://dotwebstack.org/wsdl-extension/";
  private static final String DWS_INFOPROD = "informationProduct";

  private final Definition wsdlDefinition;

  private final Port wsdlPort;

  private static final String ERROR_RESPONSE = "<?xml version=\"1.0\"?>"
      + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
      + "  <s:Body>"
      + "    <s:Fault>"
      + "      <faultcode xmlns:a=\"http://schemas.microsoft.com/ws/2005/05/addressing/none\">"
      + "a:ActionNotSupported</faultcode>"
      + "      <faultstring xml:lang=\"en-US\">The message with Action '%s' cannot be processed "
      + "at the receiver, due to a ContractFilter mismatch at the EndpointDispatcher. This may "
      + "be because of either a contract mismatch (mismatched Actions between sender and "
      + "receiver) or a binding/security mismatch between the sender and the receiver.  Check "
      + "that sender and receiver have the same contract and the same binding (including "
      + "security requirements, e.g. Message, Transport, None).</faultstring>"
      + "    </s:Fault>"
      + "  </s:Body>"
      + "</s:Envelope>";

  public SoapRequestHandler(@NonNull Definition wsdlDefinition, @NonNull Port wsdlPort) {
    this.wsdlPort = wsdlPort;
    this.wsdlDefinition = wsdlDefinition;
  }

  @Override
  public String apply(ContainerRequestContext data) {
    final String soapAction = data.getHeaderString("SOAPAction");
    String msg = ERROR_RESPONSE;
    LOG.debug("Handling SOAP request, SOAPAction: {}",soapAction);

    try {

      // Scan the defined service and message-definition in order to
      // find the received action request.
      Map<String, Service> wsdlServices = wsdlDefinition.getServices();
      for (Service wsdlService : wsdlServices.values()) {
        System.out.println("- Service: " + wsdlService.getQName().getLocalPart());
        Map<String, Port> wsdlPorts = wsdlService.getPorts();
        for (Port wsdlPort : wsdlPorts.values()) {
          System.out.println("  - Port: " + wsdlPort.getName());
          List<ExtensibilityElement> wsdlElements = wsdlPort.getExtensibilityElements();
          for (ExtensibilityElement wsdlElement : wsdlElements) {
            System.out.println("    - LocationURI: " + SoapUtils.getLocationUri(wsdlElement));
          }
          List<BindingOperation> wsdlBindingOperations =
              wsdlPort.getBinding().getBindingOperations();
          for (BindingOperation wsdlBindingOperation : wsdlBindingOperations) {
            System.out.println("    - BindingOperation: " + wsdlBindingOperation.getName());
            System.out.println("    - Comparing to: " + soapAction);

            // Skip this binding operation if it does not match the required action.
            if (soapAction.endsWith("/" + wsdlBindingOperation.getName())) {
              continue;
            }

            System.out.println("    - found BindingOperation: " + wsdlBindingOperation.getName());

            Element docElement = wsdlBindingOperation.getOperation().getDocumentationElement();
            /*
            System.out.println("--- Documentation ---");
            SoapUtils.printElement(docElement);
            System.out.println("--- Documentation ---");
            */
            if (docElement.hasAttributeNS(DWS_NAMESPACE,DWS_INFOPROD)) {
              System.out.println("      - Informationproduct: "
                  + docElement.getAttributeNS(DWS_NAMESPACE,DWS_INFOPROD));
            }
            Map<String, Part> wsdlInputParts =
                wsdlBindingOperation.getOperation().getInput().getMessage().getParts();
            for (Part wsdlPart : wsdlInputParts.values()) {
              System.out.println("      - Input:  " + wsdlPart.getElementName());
            }
            Map<String, Part> wsdlOutputParts =
                wsdlBindingOperation.getOperation().getOutput().getMessage().getParts();
            for (Part wsdlPart : wsdlOutputParts.values()) {
              System.out.println("      - Output: " + wsdlPart.getElementName());
            }

            //Build the SOAP response for the specific message
            System.out.println("========");
            msg = SoapUtils.buildSoapMessageFromOutput(new SchemaDefinitionWrapper(
                wsdlDefinition,"http://incorrect"),
                wsdlPort.getBinding(),
                wsdlBindingOperation,
                SoapContext.DEFAULT);
            System.out.println(msg);
            System.out.println("========");
            return msg;
          }
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    // No operation found. Return the error message.
    return msg;
  }
}
