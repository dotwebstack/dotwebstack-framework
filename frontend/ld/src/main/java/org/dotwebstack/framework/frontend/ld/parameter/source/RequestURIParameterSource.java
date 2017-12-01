package org.dotwebstack.framework.frontend.ld.parameter.source;

import javax.ws.rs.container.ContainerRequestContext;

public class RequestURIParameterSource implements ParameterSource {

  public String getValue(ContainerRequestContext containerRequestContext) {
    return containerRequestContext.getUriInfo().getAbsolutePath().getRawPath();
  }
}
