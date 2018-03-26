package org.dotwebstack.framework.transaction.flow.step.update;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTransactionRepositoryExecutor extends AbstractStepExecutor<UpdateStep> {

  private static final Logger LOG =
      LoggerFactory.getLogger(UpdateTransactionRepositoryExecutor.class);

  private RepositoryConnection transactionConnection;

  public UpdateTransactionRepositoryExecutor(UpdateStep updateStep,
      @NonNull RepositoryConnection transactionConnection) {
    super(updateStep);
    this.transactionConnection = transactionConnection;
  }

  @Override
  public void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    Update preparedQuery;
    String query = step.getQuery();

    try {
      preparedQuery = transactionConnection.prepareUpdate(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s (%s)", query,
          e.getMessage()), e);
    }

    bindParameters(parameters, parameterValues).forEach(preparedQuery::setBinding);

    try {
      preparedQuery.execute();
      LOG.debug("Executed Update step {}", query);
    } catch (QueryEvaluationException e) {
      throw new BackendException(String.format("Query could not be executed: %s (%s)", query,
          e.getMessage()), e);
    }
  }


}
