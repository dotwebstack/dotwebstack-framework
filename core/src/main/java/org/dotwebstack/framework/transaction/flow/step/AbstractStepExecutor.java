package org.dotwebstack.framework.transaction.flow.step;

public abstract class AbstractStepExecutor<T> implements StepExecutor {

  protected T step;

  public AbstractStepExecutor(T step) {
    this.step = step;
  }

}
