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
    redirectionTemplate = "$1" + incrementRegexObjects(redirectionTemplate);

    String redirectPath = path.replaceAll(urlPattern, redirectionTemplate);

    try {
      URI redirectUri = new URI(redirectPath);

      return Response.seeOther(redirectUri).location(redirectUri).build();
    } catch (URISyntaxException e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  private String incrementRegexObjects(String input) {
    Pattern digitPattern = Pattern.compile("(\\$\\d+)");

    Matcher matcher = digitPattern.matcher(input);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(result,
          "\\$" + String.valueOf(Integer.parseInt(matcher.group(1).substring(1)) + 1));
    }
    matcher.appendTail(result);

    return result.toString();
  }
}
