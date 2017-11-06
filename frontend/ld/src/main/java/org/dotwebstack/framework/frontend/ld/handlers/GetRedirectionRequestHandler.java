package org.dotwebstack.framework.frontend.ld.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.redirection.Redirection;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRedirectionRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRedirectionRequestHandler.class);

  private Redirection redirection;

  public GetRedirectionRequestHandler(@NonNull Redirection redirection) {
    this.redirection = redirection;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();

    LOG.debug("Handling GET redirect for path {}", path);

    String urlPattern = redirection.getUrlPattern().replaceAll("\\^", "(.*)");

    String redirectionTemplate = redirection.getRedirectionTemplate();
    redirectionTemplate = "$1"
        + redirectionTemplate.replaceAll("\\$9", "\\$10").replaceAll("\\$8", "\\$9").replaceAll(
            "\\$7", "\\$8").replaceAll("\\$6", "\\$7").replaceAll("\\$5", "\\$6").replaceAll("\\$4",
                "\\$5").replaceAll("\\$3", "\\$4").replaceAll("\\$2", "\\$3").replaceAll("\\$1",
                    "\\$2");

    String redirectPath = path.replaceAll(urlPattern, redirectionTemplate);

    try {
      URI redirectUri = new URI(redirectPath);

      return Response.seeOther(redirectUri).location(redirectUri).build();
    } catch (URISyntaxException e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
