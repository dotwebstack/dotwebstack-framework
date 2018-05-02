package org.dotwebstack.framework.backend.sparql.persistencestep;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;

public class PersistenceInsertIntoGraphStepExecutor extends AbstractStepExecutor<PersistenceStep> {

  private SparqlBackend backend;

  private Model transactionModel;

  private PersistenceStep persistenceStep;

  private QueryEvaluator queryEvaluator;

  private ApplicationProperties applicationProperties;

  public PersistenceInsertIntoGraphStepExecutor(@NonNull PersistenceStep persistenceStep,
      @NonNull Model transactionModel, @NonNull SparqlBackend backend,
      @NonNull QueryEvaluator queryEvaluator,
      @NonNull ApplicationProperties applicationProperties) {
    super(persistenceStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
    this.persistenceStep = persistenceStep;
    this.queryEvaluator = queryEvaluator;
    this.applicationProperties = applicationProperties;
  }

  @Override
  public void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    queryEvaluator.add(backend.getConnection(), transactionModel, applicationProperties);
  }

}
