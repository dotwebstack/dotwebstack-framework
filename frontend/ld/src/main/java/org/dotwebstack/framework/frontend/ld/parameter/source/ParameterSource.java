package org.dotwebstack.framework.frontend.ld.parameter.source;

import javax.ws.rs.container.ContainerRequestContext;

public interface ParameterSource {

  String getValue(ContainerRequestContext containerRequestContext);
}
