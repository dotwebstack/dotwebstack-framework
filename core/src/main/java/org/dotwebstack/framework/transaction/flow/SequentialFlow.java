package org.dotwebstack.framework.transaction.flow;

import org.dotwebstack.framework.transaction.flow.step.Step;

public class SequentialFlow extends AbstractFlow {

  public void execute() {
    transactionSteps.forEach(step -> step.execute());
  }

}
