package org.dotwebstack.framework.backend;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface Backend {

  IRI getIdentifier();

  BackendSource createSource(Model statements);

}
