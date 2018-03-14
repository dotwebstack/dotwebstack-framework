package org.dotwebstack.framework.backend.sparql.updatestep;

import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.update.UpdateStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendUpdateStepFactory {

  private QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendUpdateStepFactory(@NonNull QueryEvaluator queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }

  public UpdateStepExecutor create(@NonNull UpdateStep updateStep,
      @NonNull SparqlBackend sparqlBackend) {
    return new UpdateStepExecutor(updateStep, queryEvaluator, sparqlBackend);
  }

}
