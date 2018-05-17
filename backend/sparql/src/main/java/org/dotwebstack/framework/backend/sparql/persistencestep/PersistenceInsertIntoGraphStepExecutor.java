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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceInsertIntoGraphStepExecutor extends AbstractStepExecutor<PersistenceStep> {

  private static final Logger LOG =
      LoggerFactory.getLogger(PersistenceInsertIntoGraphStepExecutor.class);

  private SparqlBackend backend;

  private Model transactionModel;

  private QueryEvaluator queryEvaluator;

  private ApplicationProperties applicationProperties;

  public PersistenceInsertIntoGraphStepExecutor(@NonNull PersistenceStep persistenceStep,
      @NonNull Model transactionModel, @NonNull SparqlBackend backend,
      @NonNull QueryEvaluator queryEvaluator,
      @NonNull ApplicationProperties applicationProperties) {
    super(persistenceStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
    this.queryEvaluator = queryEvaluator;
    this.applicationProperties = applicationProperties;
  }

  @Override
  public void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    try {
      if (step.getTargetGraph() != null) {
        LOG.debug("Execute persistence step {} with targetGraph {}", step.getIdentifier(),
            step.getTargetGraph());
        queryEvaluator.add(backend.getConnection(), transactionModel, step.getTargetGraph());
        LOG.debug("Added data into backend {} with graph {} by persistence step {}",
            backend.getIdentifier(), step.getTargetGraph(), step.getIdentifier());
      } else {
        LOG.debug("Execute persistence step {} with systemGraph {}", step.getIdentifier(),
            applicationProperties.getSystemGraph());
        queryEvaluator.add(backend.getConnection(), transactionModel,
            applicationProperties.getSystemGraph());
        LOG.debug("Added data into backend {} with graph {} by persistence step {}",
            backend.getIdentifier(), applicationProperties.getSystemGraph(), step.getIdentifier());
      }
    } catch (Exception ex) {
      LOG.debug("Get error {} for persistence step {}", ex, step.getIdentifier());
    }
  }

}
