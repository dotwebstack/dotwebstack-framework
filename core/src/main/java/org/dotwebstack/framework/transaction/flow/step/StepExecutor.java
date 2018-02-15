package org.dotwebstack.framework.transaction.flow.step;

import org.eclipse.rdf4j.repository.Repository;

public interface StepExecutor<T> {

  void execute(Repository repository);
}
