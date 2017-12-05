package org.dotwebstack.framework.frontend.ld.parameter.source;

import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import org.dotwebstack.framework.vocabulary.HTTP;
import org.eclipse.rdf4j.model.IRI;

public class RequestUriParameterSource implements ParameterSource {
  public static IRI getIRI() {
    return HTTP.REQUEST_URI;
  }

  public String getValue(ContainerRequestContext containerRequestContext) {
    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();

    return String.format("%s://%s%s", uri.getScheme(), uri.getHost(), uri.getPath());
  }
}
