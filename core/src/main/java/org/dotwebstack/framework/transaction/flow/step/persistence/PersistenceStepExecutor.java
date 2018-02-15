package org.dotwebstack.framework.transaction.flow.step.persistence;

import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

public class PersistenceStepExecutor extends AbstractStepExecutor<PersistenceStep> {

  public PersistenceStepExecutor(
      PersistenceStep step, RepositoryConnection repositoryConnection) {
    super(step, repositoryConnection);
  }

  @Override
  public void execute() {
    // now only do insert or update
    // get all statements, ignoring context
    RepositoryResult<Statement> statements = repositoryConnection.getStatements(null, null, null);

    //
    // step.getBackend().
  }
}
