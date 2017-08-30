package org.dotwebstack.framework.frontend.http;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
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
    UriBuilder hostUriBuilder = uriInfo.getBaseUriBuilder();

    // get host from header forwarded host if set
    String host = requestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST);
    if (host != null) {
      hostUriBuilder.uri("http://" + host);
    }

    UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri() +
        hostUriBuilder.build().getHost() + "/" + uriInfo.getPath());

    LOG.debug("Set new request path to {} (was {})", uriBuilder, uriInfo.getAbsolutePath());

    requestContext.setRequestUri(uriBuilder.build());
  }

}
