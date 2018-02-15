package org.dotwebstack.framework.transaction.flow.step;

public interface Step<T> {

  StepExecutor<T> createStepExecutor();

}
