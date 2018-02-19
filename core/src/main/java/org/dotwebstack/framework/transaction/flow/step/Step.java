package org.dotwebstack.framework.transaction.flow.step;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface Step<T> {

  StepExecutor<T> createStepExecutor(RepositoryConnection repositoryConnection);

}
