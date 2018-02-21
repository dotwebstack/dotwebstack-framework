package org.dotwebstack.framework.backend.sparql.persistencestep;

import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;

public class PersistenceInsertIntoGraphStepExecutor extends AbstractStepExecutor<PersistenceStep> {

  private SparqlBackend backend;

  private Model transactionModel;

  private PersistenceStep persistenceStep;

  public PersistenceInsertIntoGraphStepExecutor(PersistenceStep persistenceStep,
      Model transactionModel, SparqlBackend backend) {
    super(persistenceStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
    this.persistenceStep = persistenceStep;
  }

  public void execute() {
    // add statements to graph
    backend.getConnection().add(transactionModel, persistenceStep.getTargetGraph());
  }

}
