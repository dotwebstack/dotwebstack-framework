package org.dotwebstack.framework.backend;

import org.eclipse.rdf4j.model.Model;

public interface BackendSourceFactory {

  BackendSource create(Backend backend, Model statements);

}
