package org.dotwebstack.framework.transaction.flow;

import java.util.List;
import org.dotwebstack.framework.transaction.flow.step.Step;

public abstract class AbstractFlow implements Flow {

  protected List<Step> transactionSteps;
}
