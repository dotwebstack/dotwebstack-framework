package org.dotwebstack.framework.frontend.http;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class PoweredByResponseFilter implements ContainerResponseFilter {

  private static final Logger LOG = LoggerFactory.getLogger(PoweredByResponseFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {

    responseContext.getHeaders().add("X-Powered-By", "Dotwebstack");
  }
}
