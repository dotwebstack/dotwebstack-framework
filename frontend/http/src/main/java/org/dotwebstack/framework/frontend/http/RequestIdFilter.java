package org.dotwebstack.framework.frontend.http;

import java.io.IOException;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;

@PreMatching
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final String X_REQUEST_ID_HEADER = "X-Request-ID";
  public static final String REQUEST_ID_PROPERTY = "REQUEST_ID";

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    String requestId = (String) requestContext.getProperty(REQUEST_ID_PROPERTY);
    responseContext.getHeaders().add(X_REQUEST_ID_HEADER, requestId);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String requestId = UUID.randomUUID().toString();
    requestContext.setProperty(REQUEST_ID_PROPERTY, requestId);
  }

}
