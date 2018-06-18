package org.dotwebstack.framework.frontend.soap.mappers;

import static javax.ws.rs.HttpMethod.POST;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import lombok.NonNull;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.soap.SoapUtils;
import org.dotwebstack.framework.frontend.soap.handlers.SoapRequestHandler;
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
import org.xml.sax.InputSource;

@Service
public class SoapRequestMapper implements ResourceLoaderAware, EnvironmentAware {

  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestMapper.class);

  // private final SwaggerParser openApiParser; SHOULD BE WsdlParser
  private WSDLReader wsdlReader;

  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  private Environment environment;

  private List<RequestMapper> requestMappers;

  @Autowired
  public SoapRequestMapper(
      @NonNull ApplicationProperties applicationProperties,
      @NonNull List<RequestMapper> requestMappers) {
    this.applicationProperties = applicationProperties;
    this.requestMappers = requestMappers;
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

    LOG.debug("Loading files from {}",applicationProperties.getResourcePath());
    try {
      resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
          applicationProperties.getResourcePath() + "/soap/**.xml");
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
          Definition wsdlDefinition = wsdlReader.readWSDL("http://example.org/",
              new InputSource(new StringReader(result)));
          mapSoapDefinition(wsdlDefinition, httpConfiguration);
        } catch (WSDLException exp) {
          LOG.error("Error reading WSDL file: {}", resource.getDescription());
          throw new IOException(exp);
        }
      }
    }
  }

  private void mapSoapDefinition(Definition wsdlDefinition,
      HttpConfiguration httpConfiguration) {
    Map<String, javax.wsdl.Service> wsdlServices = wsdlDefinition.getServices();
    for (javax.wsdl.Service wsdlService : wsdlServices.values()) {
      Map<String, Port> wsdlPorts = wsdlService.getPorts();
      for (Port wsdlPort : wsdlPorts.values()) {
        List<ExtensibilityElement> wsdlElements = wsdlPort.getExtensibilityElements();
        for (ExtensibilityElement wsdlElement : wsdlElements) {
          try {
            URI locationUri = new URI(SoapUtils.getLocationUri(wsdlElement));
            String servicePath = String.format("/%s%s", locationUri.getHost(),
                locationUri.getPath());
            LOG.debug("Register resource path {} for SOAP service {}", servicePath,
                locationUri.toString());
            Builder soapResourceBuilder = Resource.builder().path(servicePath);
            soapResourceBuilder
                .addMethod(POST)
                .produces("application/soap+xml")
                .handledBy(new SoapRequestHandler(wsdlDefinition, wsdlPort));
            httpConfiguration.registerResources(soapResourceBuilder.build());
          } catch (URISyntaxException exp) {
            LOG.warn("Location URI in WSDL could not be parsed: {}",
                SoapUtils.getLocationUri(wsdlElement));
          }
        }
      }
    }
  }

}
