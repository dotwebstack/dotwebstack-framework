package org.dotwebstack.framework.frontend.soap;

import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapRequestHandler implements Inflector<ContainerRequestContext, String> {

  private static final Logger LOG = LoggerFactory.getLogger(SoapRequestHandler.class);

  private static final String httpResponseBody = "<?xml version=\"1.0\"?>"
      + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\">"
      + "  <soap:Body xmlns:m=\"http://www.example.org/soaptest\">"
      + "    <m:GetTestResult>"
      + "      <m:Value>TEST!</m:Value>"
      + "    </m:GetTestResult>"
      + "  </soap:Body>"
      + "</soap:Envelope>";
  
  public SoapRequestHandler() {
  }

  @Override
  public String apply(ContainerRequestContext data) {
    LOG.debug("Handling SOAP request");
    return httpResponseBody;
  }

}
