package org.dotwebstack.framework.frontend.ld.parameter.source;

import javax.ws.rs.container.ContainerRequestContext;

public class RequestURIParameterSource implements ParameterSource {

  private ContainerRequestContext containerRequestContext;

  public RequestURIParameterSource(ContainerRequestContext containerRequestContext) {
    this.containerRequestContext = containerRequestContext;
  }

  public String getValue() {
    return containerRequestContext.getUriInfo().getAbsolutePath().getRawPath();
  }
}
