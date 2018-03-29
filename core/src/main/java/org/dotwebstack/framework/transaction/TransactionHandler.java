package org.dotwebstack.framework.transaction;

import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class TransactionHandler {

  private Transaction transaction;

  private Model model;

  private Repository transactionRepository;

  public TransactionHandler(@NonNull Repository transactionRepository,
      @NonNull Transaction transaction, @NonNull Model model) {
    this.transaction = transaction;
    this.model = model;
    this.transactionRepository = transactionRepository;
  }

  public void execute(@NonNull Map<String, String> parameterValues) {
    transactionRepository.initialize();
    RepositoryConnection repositoryConnection = transactionRepository.getConnection();
    repositoryConnection.add(model);

    transaction.getFlow().getExecutor(repositoryConnection).execute(transaction.getParameters(),
        parameterValues);

    repositoryConnection.close();
    transactionRepository.shutDown();
  }

}
