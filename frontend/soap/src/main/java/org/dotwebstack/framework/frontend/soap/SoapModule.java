package org.dotwebstack.framework.frontend.soap;

import static javax.ws.rs.HttpMethod.GET;

import java.io.IOException;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.dotwebstack.framework.frontend.soap.mappers.SoapRequestMapper;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.Resource.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SoapModule implements HttpModule {

  private static final Logger LOG = LoggerFactory.getLogger(SoapModule.class);

  private SoapRequestMapper requestMapper;

  @Autowired
  public SoapModule(@NonNull SoapRequestMapper requestMapper) {
    this.requestMapper = requestMapper;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    
    //Simple test, should be in a separate Mapper class
    /*
    String absolutePath = "/localhost/soaptest";
    Builder soapResourceBuilder = Resource.builder().path(absolutePath);
    soapResourceBuilder//
        .addMethod(GET)//
        .produces("application/soap+xml")//
        .handledBy(new SoapRequestHandler());
    httpConfiguration.registerResources(soapResourceBuilder.build());
    LOG.debug("Mapped GET operation for request path {}", absolutePath);
    */
    
    try {
      requestMapper.map(httpConfiguration);
    } catch (IOException exp) {
      LOG.error("IOException, message " + exp.getMessage());
      throw new ConfigurationException("Failed loading SOAP-WSDL definitions.", exp);
    }
  }

}
