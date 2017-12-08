package org.dotwebstack.framework.frontend.ld.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.redirection.Redirection;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectionRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectionRequestHandler.class);

  private Redirection redirection;

  public RedirectionRequestHandler(@NonNull Redirection redirection) {
    this.redirection = redirection;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    /*
     * Remove first 'domain' part of path that we have added in HostPreMatchingRequestFilter
     */
    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();
    String path = uri.getPath().replaceAll("^/" + uri.getHost(), "");

    LOG.debug("Handling GET redirect for path {}", path);

    String urlPattern = redirection.getUrlPattern().replaceAll("\\^", "(.*)");

    String targetUrl = redirection.getTargetUrl();
    targetUrl = "$1" + incrementRegexObjects(targetUrl);

    String redirectPath = path.replaceAll(urlPattern, targetUrl);

    try {
      URI redirectUri = new URI(redirectPath);

      return Response.seeOther(redirectUri).location(redirectUri).build();
    } catch (URISyntaxException e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  private String incrementRegexObjects(String input) {
    Pattern regexObjectPattern = Pattern.compile("(\\$\\d+)");

    Matcher matcher = regexObjectPattern.matcher(input);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(result,
          "\\$" + (Integer.parseInt(matcher.group(1).substring(1)) + 1));
    }
    matcher.appendTail(result);

    return result.toString();
  }
}
