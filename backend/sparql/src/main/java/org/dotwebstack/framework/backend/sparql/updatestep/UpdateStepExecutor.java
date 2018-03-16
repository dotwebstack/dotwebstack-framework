package org.dotwebstack.framework.backend.sparql.updatestep;

import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.update.UpdateStep;

public class UpdateStepExecutor extends AbstractStepExecutor<UpdateStep> {

  private final QueryEvaluator queryEvaluator;

  private final SparqlBackend sparqlBackend;

  public UpdateStepExecutor(UpdateStep step,
      @NonNull QueryEvaluator queryEvaluator,
      @NonNull SparqlBackend sparqlBackend) {
    super(step);
    this.queryEvaluator = queryEvaluator;
    this.sparqlBackend = sparqlBackend;
  }

  @Override
  public void execute() {
    queryEvaluator.update(sparqlBackend.getConnection(), step.getQuery());
  }

}
