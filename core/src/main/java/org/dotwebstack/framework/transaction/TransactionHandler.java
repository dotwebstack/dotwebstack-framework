package org.dotwebstack.framework.transaction;

import org.dotwebstack.framework.transaction.flow.FlowExecutor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class TransactionHandler {

  private Transaction transaction;

  private Model model;

  private Repository transactionRepository;

  public TransactionHandler(Transaction transaction, Model model) {
    this.transaction = transaction;
    this.model = model;
  }

  public void execute() {
    transactionRepository = new SailRepository(new MemoryStore());
    transactionRepository.initialize();
    RepositoryConnection repositoryConnection = transactionRepository.getConnection();
    repositoryConnection.add(model);

    FlowExecutor flowExecutor = transaction.getFlow().getExecutor(repositoryConnection);
    flowExecutor.execute();

    repositoryConnection.close();
    transactionRepository.shutDown();
  }

}
