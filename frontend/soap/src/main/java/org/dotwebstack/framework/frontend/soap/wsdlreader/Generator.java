package org.dotwebstack.framework.frontend.soap.wsdlreader;

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
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
import org.w3c.dom.Element;

public class Generator {

  private static final String DWS_NAMESPACE = "http://dotwebstack.org/wsdl-extension/";
  private static final String DWS_INFOPROD = "informationProduct";

  public static void main(String[] args) {

    try {
      // Read the wsdl
      System.out.println("SOAP generator, starting");
      URL wsdlUrl = new File("test-wsdl.xml").toURI().toURL();
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      //uncomment the line below, if you don't want any verbose output
      //wsdlReader.setFeature("javax.wsdl.verbose", false);

      Definition wsdlDefinition = null;
      try {
        wsdlDefinition = wsdlReader.readWSDL(wsdlUrl.toString());
      } catch (WSDLException exception) {
        System.out.println("Error loading file");
        System.out.println("Fault Code: " + exception.getFaultCode());
        System.out.println("Location: " + exception.getLocation());
        System.out.println("Message: " + exception.getMessage());
      }          

      // Definition wsdlDefinition = wsdlReader.readWSDL(wsdlUrl.toString());
      System.out.println("File loaded");
      System.out.println("=======================");
      
      //Output the types defined in the wsdl
      System.out.println("Types:");
      Types wsdlTypes = wsdlDefinition.getTypes();
      List<ExtensibilityElement> elements = wsdlTypes.getExtensibilityElements();
      for (ExtensibilityElement element : elements) {
        if (element instanceof SchemaImpl) {
          Map<String, List> includes = ((SchemaImpl) element).getImports();
          for (List<SchemaImport> includeList : includes.values()) {
            for (SchemaImport schemaImport : includeList) {
              Schema schema = schemaImport.getReferencedSchema();
              SoapUtils.printSchema(wsdlDefinition, schema);
            }
          }
          SoapUtils.printSchema(wsdlDefinition, (Schema) element);
        } 
      }
      System.out.println("=======================");
      
      //Output the defined service and message-definition
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
            String msg =
                SoapUtils.buildSoapMessageFromOutput(
                new SchemaDefinitionWrapper(wsdlDefinition, wsdlUrl.toString()),
                wsdlPort.getBinding(),
                wsdlBindingOperation,
                SoapContext.DEFAULT);
            System.out.println(msg);
            System.out.println("========");
          }
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

  }
}
