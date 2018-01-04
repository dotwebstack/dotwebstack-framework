package org.dotwebstack.framework.param;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface ParameterResourceFactory {

  ParameterDefinition create(Model backendModel, IRI identifier);

  boolean supports(IRI backendType);
}
