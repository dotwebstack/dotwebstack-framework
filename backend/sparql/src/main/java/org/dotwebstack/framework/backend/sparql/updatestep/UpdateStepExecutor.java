package org.dotwebstack.framework.backend.sparql.updatestep;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.param.Parameter;
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
  public void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    queryEvaluator.update(sparqlBackend.getConnection(), step.getQuery());
  }

}
