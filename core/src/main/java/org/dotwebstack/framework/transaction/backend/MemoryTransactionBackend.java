package org.dotwebstack.framework.transaction.backend;

import org.eclipse.rdf4j.model.Model;
import org.springframework.stereotype.Service;

@Service
public class MemoryTransactionBackend implements TransactionBackend {

  @Override
  public void store(Model model, String graphName) {

  }

  @Override
  public Model get(String graphname) {
    return null;
  }

  @Override
  public void update(String graphname) {

  }
}
