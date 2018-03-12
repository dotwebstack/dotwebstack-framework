package org.dotwebstack.framework.transaction.flow.step.update;

import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class UpdateTransactionRepositoryExecutor extends AbstractStepExecutor<UpdateStep> {

  private RepositoryConnection transactionConnection;

  public UpdateTransactionRepositoryExecutor(UpdateStep updateStep,
      RepositoryConnection transactionConnection) {
    super((updateStep));
    this.transactionConnection = transactionConnection;
  }

  @Override
  public void execute() {
    Query preparedQuery = transactionConnection.prepareQuery(QueryLanguage.SPARQL, step.getQuery());

    if (preparedQuery instanceof GraphQuery) {
      try {
        ((GraphQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(
            String.format("Query could not be evaluated: %s", step.getQuery()), e);
      }
    }

    if (preparedQuery instanceof TupleQuery) {
      try {
        ((TupleQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(
            String.format("Query could not be evaluated: %s", step.getQuery()), e);
      }
    }

    throw new BackendException(
        String.format("Query type '%s' not supported.", preparedQuery.getClass()));
  }

}
