package org.dotwebstack.framework.frontend.soap.wsdlreader;

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;

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
 * <p/>
 * Changes done:
 * - changing location in the package structure
 * - removal of dependencies and code parts that are out of scope of SOAP message generation
 * - minor fixes to make the class compile out of soapUI's code base
 */
class DefinitionSchemaLoader implements SchemaLoader, DefinitionLoader {
  private String baseUri;
  private Definition wsdlDefinition;

  public DefinitionSchemaLoader(Definition wsdlDefinition) {
    this.baseUri = wsdlDefinition.getDocumentBaseURI();
    this.wsdlDefinition = wsdlDefinition;
  }

  public XmlObject loadXmlObject(String wsdlUrl, XmlOptions options) throws Exception {
    //First try: only one schema
    SchemaImpl schema = null;
    List<ExtensibilityElement> elements = wsdlDefinition.getTypes().getExtensibilityElements();
    for (ExtensibilityElement element : elements) {
      if (element instanceof SchemaImpl) {
        schema = (SchemaImpl) element;
      }
    }
    //Namespaces overzetten naar schema
    Element schemaElement = (Element) schema.getElement().getParentNode();
    Map<String, String> namespaces = wsdlDefinition.getNamespaces();
    for (Entry<String, String> entry : namespaces.entrySet()) {
      if (entry.getKey().equals("xmlns") || entry.getKey().trim().isEmpty()) {
        continue;
      }
      if (schemaElement.getAttribute("xmlns:" + entry.getKey()).isEmpty()) {
        schemaElement.setAttribute("xmlns:" + entry.getKey(), entry.getValue());
      }
    }
    return XmlObject.Factory.parse(schemaElement, options);
    //return XmlUtils.createXmlObject(new URL(wsdlUrl), options);
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setProgressInfo(String info) {
    throw new SoapBuilderException("Not Implemented");
  }

  public boolean isAborted() {
    throw new SoapBuilderException("Not Implemented");
  }

  public boolean abort() {
    throw new SoapBuilderException("Not Implemented");
  }

  public void setNewBaseUri(String uri) {
    throw new SoapBuilderException("Not Implemented");
  }

  public String getFirstNewUri() {
    throw new SoapBuilderException("Not Implemented");
  }
}
