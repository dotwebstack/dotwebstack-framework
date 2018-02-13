package org.dotwebstack.framework.transaction.flow;

import java.util.List;
import org.dotwebstack.framework.transaction.flow.step.Step;

public class SequentialFlow implements Flow {

  private List<Step> steps;

  public SequentialFlow(Builder builder) {
    this.steps = builder.steps;
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
