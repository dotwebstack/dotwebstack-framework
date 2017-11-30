package org.dotwebstack.framework.frontend.ld.parameter.source;

import javax.ws.rs.container.ContainerRequestContext;
import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.IRI;

public class ParameterSourceFactory {

  private static final String RequestIRI = "http://www.w3.org/2011/http#requestURI";

  private ContainerRequestContext containerRequestContext;

  public ParameterSourceFactory(ContainerRequestContext containerRequestContext) {
    this.containerRequestContext = containerRequestContext;
  }

  public ParameterSource getParameterSource(IRI iri) {
    if (iri.equals(RequestIRI)) {
      return new RequestURIParameterSource(containerRequestContext);
    }

    throw new ConfigurationException(String.format("Parameter source %s is not supported", iri));
  }
}
