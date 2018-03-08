package org.dotwebstack.framework.transaction.flow;

import lombok.NonNull;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public abstract class AbstractFlowExecutor<T> implements FlowExecutor {

  protected T flow;

  RepositoryConnection repositoryConnection;

  public AbstractFlowExecutor(@NonNull T flow,
      @NonNull RepositoryConnection repositoryConnection) {
    this.flow = flow;
    this.repositoryConnection = repositoryConnection;
  }

}
