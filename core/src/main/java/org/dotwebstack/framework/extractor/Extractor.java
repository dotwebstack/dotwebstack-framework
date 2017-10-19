package org.dotwebstack.framework.extractor;


import javax.ws.rs.container.ContainerRequestContext;

public interface Extractor {

  String extract(ContainerRequestContext containerRequestContext, String parameterName);
}
