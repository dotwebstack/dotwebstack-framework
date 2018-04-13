package org.dotwebstack.framework.frontend.http;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PreMatching
public class HostPreMatchingRequestFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(HostPreMatchingRequestFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    UriInfo uriInfo = requestContext.getUriInfo();
    UriBuilder hostUriBuilder = uriInfo.getRequestUriBuilder();

    // get host from header forwarded host if set
    String forwardedHost = requestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST);
    LOG.debug("x-forwarded-host: {}", forwardedHost);
    URI builtRequestUri = hostUriBuilder.build();
    String replacementUri = builtRequestUri.getHost() + builtRequestUri.getPath();
    if (forwardedHost != null) {
      UriBuilder forwardedHostUriBuilder =
          UriBuilder.fromUri("http://" + forwardedHost.split(",")[0]);
      replacementUri = forwardedHostUriBuilder.build().getHost() + builtRequestUri.getPath();
    }
    hostUriBuilder.replacePath(replacementUri);

    LOG.debug("Set new request path to {} (was {})", hostUriBuilder, uriInfo.getAbsolutePath());

    requestContext.setRequestUri(hostUriBuilder.build());
  }

}
