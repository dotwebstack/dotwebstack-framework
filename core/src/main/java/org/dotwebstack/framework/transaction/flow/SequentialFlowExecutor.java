package org.dotwebstack.framework.transaction.flow;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class SequentialFlowExecutor extends AbstractFlowExecutor<SequentialFlow> {

  public SequentialFlowExecutor(@NonNull SequentialFlow flow,
      @NonNull RepositoryConnection repositoryConnection) {
    super(flow, repositoryConnection);
  }

  @Override
  public void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    for (Step step : flow.getSteps()) {
      step.createStepExecutor(repositoryConnection).execute(parameters, parameterValues);
    }
  }

}
