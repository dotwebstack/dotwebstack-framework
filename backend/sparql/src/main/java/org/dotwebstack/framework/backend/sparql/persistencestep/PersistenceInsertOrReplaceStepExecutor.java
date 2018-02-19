package org.dotwebstack.framework.backend.sparql.persistencestep;

import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;

public class PersistenceInsertOrReplaceStepExecutor extends AbstractStepExecutor<PersistenceStep> {

  private SparqlBackend backend;

  private Model transactionModel;

  public PersistenceInsertOrReplaceStepExecutor(PersistenceStep persistenceStep,
      Model transactionModel, SparqlBackend backend) {
    super(persistenceStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
  }

  @Override
  public void execute() {
    // todo delete statements for subject in graph

    // add statements to graph
    backend.getConnection().add(transactionModel);
  }

}
