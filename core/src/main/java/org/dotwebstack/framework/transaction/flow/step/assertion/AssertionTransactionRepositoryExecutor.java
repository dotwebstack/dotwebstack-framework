package org.dotwebstack.framework.transaction.flow.step.assertion;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertionTransactionRepositoryExecutor extends AbstractStepExecutor<AssertionStep> {

  private static final Logger LOG =
      LoggerFactory.getLogger(AssertionTransactionRepositoryExecutor.class);

  private RepositoryConnection transactionConnection;

  public AssertionTransactionRepositoryExecutor(AssertionStep assertionStep,
      @NonNull RepositoryConnection transactionConnection) {
    super(assertionStep);
    this.transactionConnection = transactionConnection;
  }

  @Override
  public void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    BooleanQuery preparedQuery;
    String query = step.getAssertionQuery();

    try {
      preparedQuery = transactionConnection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s (%s)",
          query, e.getMessage()), e);
    }

    bindParameters(parameters, parameterValues).forEach(preparedQuery::setBinding);

    try {
      boolean returnValue = preparedQuery.evaluate();
      if (step.isAssertionNot()) {
        returnValue = !returnValue;
      }
      if (!returnValue) {
        LOG.debug("Assertion {} returned false", query);
        throw new StepFailureException(step.getLabel());
      }
    } catch (QueryEvaluationException e) {
      throw new BackendException(String.format("Query could not be evaluated: %s (%s)",
          query, e.getMessage()), e);
    }
  }

}
