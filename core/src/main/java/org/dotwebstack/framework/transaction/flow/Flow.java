package org.dotwebstack.framework.transaction.flow;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface Flow {

  FlowExecutor getExecutor(RepositoryConnection repositoryConnection);

}
