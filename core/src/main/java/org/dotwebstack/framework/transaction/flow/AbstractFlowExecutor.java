package org.dotwebstack.framework.transaction.flow;

import org.eclipse.rdf4j.repository.RepositoryConnection;

public abstract class AbstractFlowExecutor<T> implements FlowExecutor<T> {

  protected T flow;

  private RepositoryConnection repositoryConnection;

  public AbstractFlowExecutor(T flow,
      RepositoryConnection repositoryConnection) {
    this.flow = flow;
    this.repositoryConnection = repositoryConnection;
  }

}
