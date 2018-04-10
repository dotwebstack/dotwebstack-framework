package org.dotwebstack.framework.frontend.http;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

@PreMatching
@Provider
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {


  public static final String X_REQUEST_ID_HEADER = "X-Request-ID";
  public static final String REQUEST_ID_PROPERTY = "REQUEST_ID";

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    String requestId = (String) requestContext.getProperty(REQUEST_ID_PROPERTY);
    responseContext.getHeaders().add(X_REQUEST_ID_HEADER, requestId);
  }


  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    Optional<String> requestIdFromRequest = requestContext.getHeaders().entrySet().stream() //
        .filter(this::hasRequestIdHeader)//
        .map(e -> e.getValue().get(0))//
        .findFirst();
    String requestId = requestIdFromRequest.orElse(UUID.randomUUID().toString());
    requestContext.setProperty(REQUEST_ID_PROPERTY, requestId);
  }


  private boolean hasRequestIdHeader(Entry<String, List<String>> e) {
    return X_REQUEST_ID_HEADER.equalsIgnoreCase(e.getKey()) && !e.getValue().isEmpty();
  }

}
