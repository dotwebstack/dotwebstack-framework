package org.dotwebstack.framework.frontend.ld.parameter.target;

import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.ParameterDefinitionResourceProvider;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TargetFactory {

  private ParameterDefinitionResourceProvider parameterDefinitionResourceProvider;

  @Autowired
  public TargetFactory(ParameterDefinitionResourceProvider parameterDefinitionResourceProvider) {
    this.parameterDefinitionResourceProvider = parameterDefinitionResourceProvider;
  }

  public Target getTarget(IRI iri) {
    if (parameterDefinitionResourceProvider.get(iri) != null) {
      return new ParameterTarget(parameterDefinitionResourceProvider.get(iri));
    }

    throw new ConfigurationException(String.format("Target parameter not found %s", iri));
  }

}
