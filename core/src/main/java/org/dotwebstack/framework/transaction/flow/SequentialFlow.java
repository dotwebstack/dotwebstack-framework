package org.dotwebstack.framework.transaction.flow;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class SequentialFlow implements Flow {

  private Resource identifier;

  private List<Step> steps;

  public SequentialFlow(@NonNull Builder builder) {
    this.identifier = builder.identifier;
    this.steps = builder.steps;
  }

  public SequentialFlowExecutor getExecutor(@NonNull RepositoryConnection repositoryConnection) {
    return new SequentialFlowExecutor(this, repositoryConnection);
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public List<Step> getSteps() {
    return steps;
  }

  public static final class Builder {

    private Resource identifier;

    private List<Step> steps;

    public Builder(@NonNull Resource identifier, @NonNull List<Step> steps) {
      this.identifier = identifier;
      this.steps = steps;
    }

    public SequentialFlow build() {
      return new SequentialFlow(this);
    }
  }
}
