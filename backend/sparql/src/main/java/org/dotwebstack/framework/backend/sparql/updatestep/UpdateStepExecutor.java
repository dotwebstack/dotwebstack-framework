package org.dotwebstack.framework.backend.sparql.updatestep;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.update.UpdateStep;
import org.eclipse.rdf4j.model.Value;

public class UpdateStepExecutor extends AbstractStepExecutor<UpdateStep> {

  private final QueryEvaluator queryEvaluator;

  private final SparqlBackend sparqlBackend;

  public UpdateStepExecutor(UpdateStep step,
      QueryEvaluator queryEvaluator,
      SparqlBackend sparqlBackend) {
    super(step);
    this.queryEvaluator = queryEvaluator;
    this.sparqlBackend = sparqlBackend;
  }

  @Override
  public void execute() {
    Map<String, Value> bindings = new HashMap<>();
    queryEvaluator.evaluate(sparqlBackend.getConnection(), step.getQuery(), bindings);
  }

}
