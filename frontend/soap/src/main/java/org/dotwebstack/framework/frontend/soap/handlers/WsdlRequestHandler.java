package org.dotwebstack.framework.frontend.soap.handlers;

import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.jersey.process.Inflector;

public class WsdlRequestHandler implements Inflector<ContainerRequestContext, String> {

  private String wsdl;

  public WsdlRequestHandler(final String wsdl) {
    this.wsdl = wsdl;
  }

  @Override
  public String apply(ContainerRequestContext data) {
    return wsdl;
  }

}
