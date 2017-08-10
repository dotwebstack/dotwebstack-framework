package org.dotwebstack.framework.backend;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface BackendFactory {

  Backend create(Model backendModel, IRI identifier);

  boolean supports(IRI backendType);

}
