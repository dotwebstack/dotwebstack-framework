package org.dotwebstack.framework.frontend.ld;

import java.util.ArrayList;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PreMatching
@Provider
public class FormatPreMatchingRequestFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(FormatPreMatchingRequestFilter.class);

  @Override
  public void filter(ContainerRequestContext containerRequestContext) {
    MultivaluedMap<String, String> queryParameters =
        containerRequestContext.getUriInfo().getQueryParameters();

    queryParameters.get("format").stream().findFirst().ifPresent(
        format -> this.setHeader(format, containerRequestContext));
  }

  private void setHeader(String format, ContainerRequestContext containerRequestContext) {
    ArrayList<String> mediaTypes = new ArrayList<>();

    switch (format) {
      case ("json"):
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaTypes.LDJSON);
        break;
      case ("xml"):
        mediaTypes.add(MediaType.APPLICATION_XML);
        mediaTypes.add(MediaTypes.RDFXML);
        break;
      case ("ttl"):
        mediaTypes.add(MediaTypes.TURTLE);
        break;
    }

    if (!mediaTypes.isEmpty()) {
      LOG.debug("Format %s set, expand headers with %s", format, mediaTypes);
      containerRequestContext.getHeaders().put(HttpHeaders.ACCEPT, mediaTypes);
    }
  }

}
