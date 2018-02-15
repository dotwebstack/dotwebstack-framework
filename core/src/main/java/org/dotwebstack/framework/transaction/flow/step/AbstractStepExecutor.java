package org.dotwebstack.framework.transaction.flow.step;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public abstract class AbstractStepExecutor<T> implements StepExecutor<T> {

  protected T step;

  protected RepositoryConnection repositoryConnection;

  public AbstractStepExecutor(T step,
      RepositoryConnection repositoryConnection) {
    this.step = step;
    this.repositoryConnection = repositoryConnection;
  }
}
