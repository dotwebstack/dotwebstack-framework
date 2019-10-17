package org.dotwebstack.framework.backend.sparql.persistencestep;

import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;

public abstract class PersistenceInsertIntoStepExecutor
    extends AbstractStepExecutor<PersistenceStep> {

  public PersistenceInsertIntoStepExecutor(PersistenceStep step) {
    super(step);
  }
}
