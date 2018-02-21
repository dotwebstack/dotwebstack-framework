package org.dotwebstack.framework.transaction.flow;

import org.dotwebstack.framework.transaction.flow.step.Step;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class SequentialFlowExecutor extends AbstractFlowExecutor<SequentialFlow> {

  public SequentialFlowExecutor(SequentialFlow flow,
      RepositoryConnection repositoryConnection) {
    super(flow, repositoryConnection);
  }

  @Override
  public void execute() {
    for (Step step : flow.getSteps()) {
      step.createStepExecutor(repositoryConnection).execute();
    }
  }

}
