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

  public static final String ADDED_DOMAIN = "addedDomain";
  private static final Logger LOG = LoggerFactory.getLogger(HostPreMatchingRequestFilter.class);


  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    UriInfo uriInfo = requestContext.getUriInfo();
    UriBuilder hostUriBuilder = uriInfo.getRequestUriBuilder();

    String addedDomain = getAddedDomain(requestContext);
    String replacementUri = addedDomain + hostUriBuilder.build().getPath();
    hostUriBuilder.replacePath(replacementUri);

    LOG.debug("Set new request path to {} (was {})", hostUriBuilder, uriInfo.getAbsolutePath());

    requestContext.setRequestUri(hostUriBuilder.build());
    requestContext.setProperty(ADDED_DOMAIN, addedDomain);
  }

  private String getAddedDomain(ContainerRequestContext containerRequestContext) {
    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();

    // get host from header forwarded host if set
    String forwardedHost = containerRequestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST);
    LOG.debug("x-forwarded-host: {}", forwardedHost);

    if (forwardedHost == null || forwardedHost.equals("")) {
      return uri.getHost();
    } else {
      // remove port if present
      return UriBuilder.fromUri("http://" + forwardedHost.split(",")[0]).build().getHost();
    }
  }
}
