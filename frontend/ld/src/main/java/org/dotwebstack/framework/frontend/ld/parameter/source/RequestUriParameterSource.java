package org.dotwebstack.framework.frontend.ld.parameter.source;

import com.google.common.net.HttpHeaders;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import org.dotwebstack.framework.vocabulary.HTTP;
import org.eclipse.rdf4j.model.IRI;

public class RequestUriParameterSource implements ParameterSource {

  public static IRI getIRI() {
    return HTTP.REQUEST_URI;
  }

  @Override
  public String getValue(ContainerRequestContext containerRequestContext) {
    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();
    String forwardedHost =
        containerRequestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST).split(",")[0];
    String toBeRemoved =
        forwardedHost == null || forwardedHost.equals("") ? uri.getHost() : forwardedHost;

    /*
     * Remove first 'domain' part of path that we have added in HostPreMatchingRequestFilter
     */
    String path = uri.getPath().replaceAll("^/" + toBeRemoved, "");

    return String.format("%s://%s%s", uri.getScheme(), uri.getHost(), path);
  }

}
