package org.dotwebstack.framework.frontend.http;

import static org.dotwebstack.framework.frontend.http.RequestIdFilter.REQUEST_ID_PROPERTY;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import org.slf4j.MDC;

@PreMatching
public class MdcRequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final String REQUEST_ID_MDC_KEY = "REQUEST_ID";

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String property = (String) requestContext.getProperty(REQUEST_ID_PROPERTY);
    MDC.put(REQUEST_ID_MDC_KEY, property);
  }

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    MDC.remove(REQUEST_ID_MDC_KEY);
  }

}
