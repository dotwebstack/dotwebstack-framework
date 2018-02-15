package org.dotwebstack.framework.transaction.flow;

import java.util.List;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class SequentialFlow implements Flow {

  private List<Step> steps;

  public SequentialFlow(Builder builder) {
    this.steps = builder.steps;
  }

  public SequentialFlowExecutor getExecutor(RepositoryConnection repositoryConnection) {
    return new SequentialFlowExecutor(this, repositoryConnection);
  }

  public List<Step> getSteps() {
    return steps;
  }

  public static final class Builder {

    private List<Step> steps;

    public Builder(List<Step> steps) {
      this.steps = steps;
    }

    public SequentialFlow build() {
      return new SequentialFlow(this);
    }
  }
}
