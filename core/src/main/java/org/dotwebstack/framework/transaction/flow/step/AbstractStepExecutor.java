package org.dotwebstack.framework.transaction.flow.step;

import lombok.NonNull;

public abstract class AbstractStepExecutor<T> implements StepExecutor {

  protected T step;

  public AbstractStepExecutor(@NonNull T step) {
    this.step = step;
  }

}
