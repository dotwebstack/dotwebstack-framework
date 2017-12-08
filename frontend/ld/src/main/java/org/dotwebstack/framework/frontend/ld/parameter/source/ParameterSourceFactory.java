package org.dotwebstack.framework.frontend.ld.parameter.source;

import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;

@Service
public class ParameterSourceFactory {

  public ParameterSource getParameterSource(IRI iri) {
    if (iri.equals(RequestUriParameterSource.getIRI())) {
      return new RequestUriParameterSource();
    }

    throw new ConfigurationException(String.format("Parameter source %s is not supported", iri));
  }

}
