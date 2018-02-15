package org.dotwebstack.framework.backend.sparql.persistencestep;

import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryResult;

public class PersistenceInsertOrReplaceStepExecutor extends AbstractStepExecutor<PersistenceStep> {

  private SparqlBackend backend;

  public PersistenceInsertOrReplaceStepExecutor(PersistenceStep persistenceStep,
      SparqlBackend backend) {
    super(persistenceStep);
    this.backend = backend;
  }

  @Override
  public void execute(Repository transactionRepository) {
    // get all statements, ignoring context
    RepositoryResult<Statement> statements = transactionRepository.getConnection()
        .getStatements(null, null, null);

    // todo delete statements for subject in graph

    // add statements to graph
    backend.getConnection().add(statements);
  }

}
