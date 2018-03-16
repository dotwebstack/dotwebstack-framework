package org.dotwebstack.framework.transaction.flow.step.update;

import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class UpdateTransactionRepositoryExecutor extends AbstractStepExecutor<UpdateStep> {

  private RepositoryConnection transactionConnection;

  public UpdateTransactionRepositoryExecutor(UpdateStep updateStep,
      RepositoryConnection transactionConnection) {
    super(updateStep);
    this.transactionConnection = transactionConnection;
  }

  @Override
  public void execute() {
    Update preparedQuery;
    String query = step.getQuery();

    try {
      preparedQuery = transactionConnection.prepareUpdate(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s", query), e);
    }

    try {
      preparedQuery.execute();
    } catch (QueryEvaluationException e) {
      throw new BackendException(String.format("Query could not be executed: %s", query), e);
    }
  }

}
