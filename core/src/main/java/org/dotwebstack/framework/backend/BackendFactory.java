package org.dotwebstack.framework.backend;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface BackendFactory {

  Backend create(Model backendModel, Resource identifier);

  boolean supports(IRI backendType);

}
