package org.dotwebstack.framework.backend.sparql.updatestep;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.param.BindableParameter;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.update.UpdateStep;
import org.eclipse.rdf4j.model.Value;

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
    Map<String, Value> bindings = new HashMap<>();

    for (Parameter<?> parameter : parameters) {
      Object value = parameter.handle(parameterValues);

      if (value != null && parameter instanceof BindableParameter) {
        bindings.put(parameter.getName(), ((BindableParameter) parameter).getValue(value));
      }
    }

    queryEvaluator.update(sparqlBackend.getConnection(), step.getQuery(), bindings);
  }

}
