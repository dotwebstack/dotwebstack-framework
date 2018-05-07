package org.dotwebstack.framework.transaction;

import java.io.StringWriter;
import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionHandler.class);

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

    StringWriter stringWriter = new StringWriter();
    RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, stringWriter);
    repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL,
        "CONSTRUCT {?s ?p ?o } WHERE {?s ?p ?o } ").evaluate(writer);
    LOG.debug("Transaction repository before starting flow execution:\n{}", stringWriter);

    transaction.getFlow().getExecutor(repositoryConnection).execute(transaction.getParameters(),
        parameterValues);

    repositoryConnection.close();
    transactionRepository.shutDown();
  }

}
