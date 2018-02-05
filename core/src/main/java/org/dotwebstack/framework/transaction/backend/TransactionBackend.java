package org.dotwebstack.framework.transaction.backend;

import org.eclipse.rdf4j.model.Model;

public interface TransactionBackend {

  void store(Model model, String graphName);

  Model get(String graphname);

  void update(String graphname);
}
