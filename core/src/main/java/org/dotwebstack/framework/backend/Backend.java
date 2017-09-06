package org.dotwebstack.framework.backend;

import org.eclipse.rdf4j.model.IRI;

public interface Backend {

  IRI getIdentifier();

  BackendSourceFactory getSourceFactory();

}
