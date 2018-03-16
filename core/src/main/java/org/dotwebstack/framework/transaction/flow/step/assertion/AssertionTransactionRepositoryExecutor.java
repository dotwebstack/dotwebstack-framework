package org.dotwebstack.framework.transaction.flow.step.assertion;

import javax.ws.rs.BadRequestException;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class AssertionTransactionRepositoryExecutor extends AbstractStepExecutor<AssertionStep> {

  private RepositoryConnection transactionConnection;

  public AssertionTransactionRepositoryExecutor(AssertionStep assertionStep,
      @NonNull RepositoryConnection transactionConnection) {
    super(assertionStep);
    this.transactionConnection = transactionConnection;
  }

  @Override
  public void execute() throws BadRequestException {
    BooleanQuery preparedQuery;
    String query = step.getAssertionQuery();

    try {
      preparedQuery = transactionConnection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s", query), e);
    }

    try {
      boolean returnValue = ((BooleanQuery)preparedQuery).evaluate();
      if (step.isAssertionNot()) {
        returnValue = !returnValue;
      }
      if (!returnValue) {
        throw new BadRequestException(String.format("Assertion failed for query %s", query));
      }
    } catch (QueryEvaluationException e) {
      throw new BackendException(String.format("Query could not be evaluated: %s", query), e);
    }
  }

}
