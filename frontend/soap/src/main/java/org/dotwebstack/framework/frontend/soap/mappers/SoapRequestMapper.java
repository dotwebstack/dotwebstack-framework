package org.dotwebstack.framework.frontend.soap.mappers;

import static javax.ws.rs.HttpMethod.POST;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.ibm.wsdl.extensions.schema.SchemaImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import lombok.NonNull;

import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.soap.action.SoapAction;
import org.dotwebstack.framework.frontend.soap.handlers.SoapRequestHandler;
import org.dotwebstack.framework.frontend.soap.wsdlreader.Constants;
import org.dotwebstack.framework.frontend.soap.wsdlreader.SoapUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.Resource.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;



@Service
public class SoapRequestMapper implements ResourceLoaderAware, EnvironmentAware {

  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestMapper.class);
  private static final String DWS_INFOPROD = "informationProduct";
  private static final String TEXT_XML = "text/xml";
  private static final String MULTIPART_RELATED = "multipart/related";
  private static final String APPLICATION_SOAP_XML = "application/soap+xml";
  private static final String UUID_CONSTANT = "uuid:e2347d89-ea40-45fd-802f-5fcc266a3858+id=1";
  private static final String APPLICATION_XOP_XML =
      "multipart/related; type=\"application/xop+xml\";start=\"<http://tempuri.org/0>\";boundary=\"" + UUID_CONSTANT + "\";start-info=\"text/xml\"";

  private WSDLReader wsdlReader;

  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  private Environment environment;

  private InformationProductResourceProvider informationProductLoader;

  @Autowired
  public SoapRequestMapper(@NonNull ApplicationProperties applicationProperties,
      @NonNull InformationProductResourceProvider informationProductLoader) {
    this.applicationProperties = applicationProperties;
    this.informationProductLoader = informationProductLoader;
    try {
      this.wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    } catch (WSDLException exp) {
      LOG.error("Error initializing WSDL reader");
    }
  }

  @Override
  public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(@NonNull Environment environment) {
    this.environment = environment;
  }

  public void map(@NonNull HttpConfiguration httpConfiguration) throws IOException {
    org.springframework.core.io.Resource[] resources;

    LOG.debug("Loading files from {}", applicationProperties.getResourcePath());
    try {
      resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
          .getResources(applicationProperties.getResourcePath() + "/soap/**.xml");
    } catch (FileNotFoundException exp) {
      LOG.warn("No Open SOAP-WSDL resources found in path:{}/soap",
          applicationProperties.getResourcePath());
      return;
    }

    if (wsdlReader != null) {
      for (org.springframework.core.io.Resource resource : resources) {
        InputStream inputStream =
            new EnvironmentAwareResource(resource.getInputStream(), environment).getInputStream();
        try {
          String result = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
          Definition wsdlDefinition =
              wsdlReader.readWSDL("http://example.org/", new InputSource(new StringReader(result)));
          mapSoapDefinition(wsdlDefinition, httpConfiguration);
        } catch (WSDLException exp) {
          LOG.error("Error reading WSDL file: {}", resource.getDescription());
          throw new IOException(exp);
        }
      }
    }
  }

  private void mapSoapDefinition(Definition wsdlDefinition, HttpConfiguration httpConfiguration) {
    Map<String, javax.wsdl.Service> wsdlServices = wsdlDefinition.getServices();
    for (javax.wsdl.Service wsdlService : wsdlServices.values()) {
      Map<String, Port> wsdlPorts = wsdlService.getPorts();
      for (Port wsdlPort : wsdlPorts.values()) {
        List<ExtensibilityElement> wsdlElements = wsdlPort.getExtensibilityElements();
        for (ExtensibilityElement wsdlElement : wsdlElements) {
          try {
            URI locationUri = new URI(SoapUtils.getLocationUri(wsdlElement));
            String servicePath =
                String.format("/%s%s", locationUri.getHost(), locationUri.getPath());
            LOG.info("Register resource path {} for SOAP service {}", servicePath, locationUri);
            registerResource(wsdlDefinition, httpConfiguration, wsdlPort, servicePath);
          } catch (URISyntaxException exp) {
            LOG.warn("Location URI in WSDL could not be parsed: {}",
                SoapUtils.getLocationUri(wsdlElement));
          }
        }
      }
    }
  }

  private void registerResource(Definition wsdlDefinition, HttpConfiguration httpConfiguration,
      Port wsdlPort, String servicePath) {
    Map<String, SoapAction> soapActions = new HashMap<>();
    createSoapActions(wsdlDefinition, wsdlPort, soapActions);

    SoapRequestHandler soapRequestHandlerXml =
        new SoapRequestHandler(wsdlDefinition, wsdlPort, soapActions, false);


    Builder soapResourceBuilderXml = Resource.builder().path(servicePath);
    soapResourceBuilderXml.addMethod(POST)
        .consumes(TEXT_XML)
        .produces(APPLICATION_SOAP_XML)
        .handledBy(soapRequestHandlerXml);
    Resource soapResourceXml = soapResourceBuilderXml.build();

    SoapRequestHandler soapRequestHandlerMtom =
        new SoapRequestHandler(wsdlDefinition, wsdlPort, soapActions, true);

    Builder soapResourceBuilderXop = Resource.builder().path(servicePath);
    soapResourceBuilderXop.addMethod(POST)
        .consumes(MULTIPART_RELATED)
        .produces(APPLICATION_XOP_XML)
        .handledBy(soapRequestHandlerMtom);


    Resource soapResourceXop = soapResourceBuilderXop.build();

    httpConfiguration
        .registerResources(soapResourceXml, soapResourceXop);
  }

  private void createSoapActions(final Definition wsdlDefinition, final Port wsdlPort,
      final Map<String, SoapAction> soapActions) {
    List<BindingOperation> wsdlBindingOperations = wsdlPort.getBinding().getBindingOperations();
    Element typesElement = findTypesElement(wsdlDefinition);

    for (BindingOperation wsdlBindingOperation : wsdlBindingOperations) {
      Element docElement = wsdlBindingOperation.getOperation().getDocumentationElement();
      if ((docElement != null) && docElement.hasAttributeNS(Constants.DWS_NS, DWS_INFOPROD)) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI informationProductIdentifier =
            valueFactory.createIRI(docElement.getAttributeNS(Constants.DWS_NS, DWS_INFOPROD));
        InformationProduct informationProduct =
            informationProductLoader.get(informationProductIdentifier);
        soapActions.put(wsdlBindingOperation.getName(),
            createSoapAction(wsdlBindingOperation.getName(), informationProduct, typesElement));
      }
    }
  }

  private Element findTypesElement(Definition wsdlDefinition) {
    Types wsdlTypes = wsdlDefinition.getTypes();
    List<ExtensibilityElement> elements = wsdlTypes.getExtensibilityElements();
    if (elements.get(0) instanceof SchemaImpl) {
      Schema schema = (Schema) elements.get(0);
      return (Element) schema.getElement().getParentNode();
    } else {
      LOG.warn("Could not find a XSD schema - no input parameters available for SOAP WSDL");
      return null;
    }
  }

  private SoapAction createSoapAction(String soapActionName, InformationProduct informationProduct,
      Element typesElement) {
    SoapAction soapAction = new SoapAction(soapActionName, informationProduct);
    soapAction.retrieveParameters(typesElement);
    return soapAction;
  }
}
