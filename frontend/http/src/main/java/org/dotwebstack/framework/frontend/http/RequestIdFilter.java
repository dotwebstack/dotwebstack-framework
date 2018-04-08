package org.dotwebstack.framework.frontend.http;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import org.slf4j.MDC;

@PreMatching
@Provider
public class RequestIdFilter implements ContainerResponseFilter, ContainerRequestFilter {


  public static final String X_REQUEST_ID_HEADER = "X-Request-ID";
  public static final String REQUEST_ID_MDC_KEY = "REQUEST_ID";


  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    String requestId = (String) requestContext.getProperty(REQUEST_ID_MDC_KEY);
    responseContext.getHeaders().add(X_REQUEST_ID_HEADER, requestId);
    MDC.clear();
  }


  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    Optional<String> requestIdFromRequest = requestContext.getHeaders().entrySet().stream() //
        .filter((e) -> X_REQUEST_ID_HEADER.equalsIgnoreCase(e.getKey()) && !e.getValue().isEmpty())//
        .map(e -> e.getValue().get(0))//
        .findFirst();
    String requestId = requestIdFromRequest.orElse(UUID.randomUUID().toString());
    MDC.put(REQUEST_ID_MDC_KEY, requestId);
    requestContext.setProperty(REQUEST_ID_MDC_KEY, requestId);
  }

}
