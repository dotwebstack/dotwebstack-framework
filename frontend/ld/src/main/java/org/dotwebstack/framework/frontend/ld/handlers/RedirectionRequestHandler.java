package org.dotwebstack.framework.frontend.ld.handlers;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.redirection.Redirection;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

public class RedirectionRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectionRequestHandler.class);

  private Redirection redirection;

  public RedirectionRequestHandler(@NonNull Redirection redirection) {
    this.redirection = redirection;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();
    String path = uri.getPath();

    LOG.debug("Handling GET redirect for path {}", path);

    UriTemplate pathPatternTemplate = new UriTemplate(redirection.getStage().getFullPath()
        + redirection.getPathPattern());
    Map<String, String> pathParameters = pathPatternTemplate.match(path);

    /*
     * Remove first 'domain' part of path that we have added in HostPreMatchingRequestFilter
     */
    String fullPath = redirection.getStage().getFullPath().replaceAll("^/" + uri.getHost(), "");
    UriTemplate redirectTemplate = new UriTemplate(fullPath + redirection.getRedirectTemplate());

    URI redirectUri = redirectTemplate.expand(pathParameters);

    return Response.seeOther(redirectUri).location(redirectUri).build();
  }

}
