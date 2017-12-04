package org.dotwebstack.framework.frontend.ld.parameter.target;

import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.ParameterResourceProvider;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;

@Service
public class TargetFactory {

  private ParameterResourceProvider parameterResourceProvider;

  public TargetFactory(ParameterResourceProvider parameterResourceProvider) {
    this.parameterResourceProvider = parameterResourceProvider;
  }

  public Target getTarget(IRI iri) {
    if (parameterResourceProvider.get(iri) != null) {
      return new ParameterTarget(parameterResourceProvider.get(iri));
    }

    throw new ConfigurationException(String.format("Target parameter not found %s", iri));
  }

}
