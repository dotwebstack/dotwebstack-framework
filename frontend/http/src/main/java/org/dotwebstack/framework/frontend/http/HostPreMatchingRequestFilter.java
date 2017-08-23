package org.dotwebstack.framework.frontend.http;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@PreMatching
@Provider
public class HostPreMatchingRequestFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(HostPreMatchingRequestFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext)
      throws IOException {
    UriInfo uriInfo = requestContext.getUriInfo();

    LOG.debug("baseURI: {}", uriInfo.getBaseUri().toString());
    LOG.debug("requestURI: {}", uriInfo.getRequestUri().toString());
    LOG.debug("Header host: {}", requestContext.getHeaderString("Host"));
    LOG.debug("Header x-forwarded-host host: {}", requestContext.getHeaderString("X-Forwarded-Host"));
//    if (requestContext.getHeaderString("X-Forwarded-Host") != null) {
//
//      URIBuilder uriBuilder = new URIBuilder().setHost(requestContext.getHeaderString("X-Forwarded-Host"));
//      uriBuilder.setPath().requestContext.getUriInfo().getAbsolutePath();
//
//      requestContext.setRequestUri(uriBuilder.ge);
//    } elseif (requestContext.getHeaderString("Host") != null) {

//    }
  }
}
