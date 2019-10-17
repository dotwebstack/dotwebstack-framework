package org.dotwebstack.framework.backend.sparql.persistencestep;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceInsertIntoGraphsStepExecutor extends PersistenceInsertIntoStepExecutor {

  private static final Logger LOG =
      LoggerFactory.getLogger(PersistenceInsertIntoGraphsStepExecutor.class);

  private SparqlBackend backend;

  private Model transactionModel;

  private QueryEvaluator queryEvaluator;

  public PersistenceInsertIntoGraphsStepExecutor(@NonNull PersistenceStep step,
      @NonNull Model transactionModel,
      @NonNull SparqlBackend backend,
      @NonNull QueryEvaluator queryEvaluator) {
    super(step);
    this.backend = backend;
    this.transactionModel = transactionModel;
    this.queryEvaluator = queryEvaluator;
  }

  @Override
  public void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    try {
      queryEvaluator.addToGraphs(backend.getConnection(), transactionModel);
    } catch (Exception e) {
      throw new StepFailureException(
          String.format("Got an error for persistence step {%s}", step.getIdentifier()));
    }
  }
}
