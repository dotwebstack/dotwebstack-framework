package org.dotwebstack.framework.backend.sparql.persistencestep;

import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;

public class PersistenceInsertIntoGraphStepExecutor extends AbstractStepExecutor<PersistenceStep> {

  private SparqlBackend backend;

  private Model transactionModel;

  private PersistenceStep persistenceStep;

  private QueryEvaluator queryEvaluator;

  public PersistenceInsertIntoGraphStepExecutor(@NonNull PersistenceStep persistenceStep,
      @NonNull Model transactionModel, @NonNull SparqlBackend backend,
      @NonNull QueryEvaluator queryEvaluator) {
    super(persistenceStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
    this.persistenceStep = persistenceStep;
    this.queryEvaluator = queryEvaluator;
  }

  @Override
  public void execute() {
    queryEvaluator.add(backend.getConnection(), transactionModel, persistenceStep.getTargetGraph());
  }

}
