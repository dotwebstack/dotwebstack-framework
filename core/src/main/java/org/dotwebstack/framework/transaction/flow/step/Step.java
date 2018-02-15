package org.dotwebstack.framework.transaction.flow.step;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface Step<T> {

  T getExecutor(RepositoryConnection repositoryConnection);

}
