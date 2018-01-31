package org.dotwebstack.framework.transaction;

import org.dotwebstack.framework.transaction.flow.Flow;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public class Transaction {

  private IRI identifier;

  private Flow flow;

  /*
   * Transaction
   */

  public void execute(Model model) {

    /*
        1. create graph with unique name (random hash) in the transaction repository
        2. load model into transaction repository into this graph
        3. execute flow
        4. remove graph
     */

    flow.execute();
  }
}
