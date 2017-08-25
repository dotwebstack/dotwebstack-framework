package org.dotwebstack.framework.frontend.http;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@PreMatching
@Provider
public class HostPreMatchingRequestFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(HostPreMatchingRequestFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext)
      throws IOException {

    // get host from header (forwarded host if set, otherwise host)
    String host = requestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST);
    if (host == null) {
      host = requestContext.getHeaderString(HttpHeaders.HOST);
    }

    // strip port
    if (host != null) {
      host = host.split(":")[0];

      // prefix host to request path
      UriInfo uriInfo = requestContext.getUriInfo();

      UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri() + host + "/" + uriInfo.getPath());
      LOG.debug("Set new request path to {} (was {})", uriBuilder.toString(), uriInfo.getAbsolutePath());

      requestContext.setRequestUri(uriBuilder.build());
    }
  }

}
