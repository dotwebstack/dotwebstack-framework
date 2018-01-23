package org.dotwebstack.framework.frontend.http;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PreMatching
@ExpandFormatParameter
public class FormatPreMatchingRequestFilter implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(FormatPreMatchingRequestFilter.class);

  @Override
  public void filter(ContainerRequestContext containerRequestContext) {
    List<String> formats = containerRequestContext.getUriInfo().getQueryParameters().get("format");
    if (formats != null) {
      formats.stream().findFirst().ifPresent(format -> setHeader(format, containerRequestContext));
    }
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
      default:
        LOG.error("Format parameter {} not supported -> ignored", format);
        break;
    }

    if (!mediaTypes.isEmpty()) {
      LOG.debug("Format parameter {} set, expand Accept header with {}", format, mediaTypes);
      containerRequestContext.getHeaders().put(HttpHeaders.ACCEPT, mediaTypes);
    }
  }

}
