package org.dotwebstack.framework.frontend.ld.parameter.source;

import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;

@Service
public class ParameterSourceFactory {

  private static final String RequestIRI = "http://www.w3.org/2011/http#requestURI";

  public ParameterSource getParameterSource(IRI iri) {
    if (iri.equals(RequestIRI)) {
      return new RequestURIParameterSource();
    }

    throw new ConfigurationException(String.format("Parameter source %s is not supported", iri));
  }
}
