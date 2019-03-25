package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.core.Backend;
import org.eclipse.rdf4j.repository.Repository;

public interface Rdf4jBackend<R extends Repository> extends Backend {

  R getRepository();

}
