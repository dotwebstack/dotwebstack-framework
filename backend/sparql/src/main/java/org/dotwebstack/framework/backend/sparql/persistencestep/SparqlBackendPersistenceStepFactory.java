package org.dotwebstack.framework.backend.sparql.persistencestep;

import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendPersistenceStepFactory {

  private QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendPersistenceStepFactory(
      QueryEvaluator queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }

  public PersistenceInsertIntoGraphStepExecutor create(@NonNull PersistenceStep persistenceStep,
      @NonNull Model transactionModel, @NonNull SparqlBackend sparqlBackend) {
    if (persistenceStep.getPersistenceStrategy().equals(
        ELMO.PERSISTENCE_STRATEGY_INSERT_INTO_GRAPH)) {
      return new PersistenceInsertIntoGraphStepExecutor(persistenceStep, transactionModel,
          sparqlBackend, queryEvaluator);
    }

    throw new ConfigurationException(String.format("Strategy %s not support by %s",
        persistenceStep.getPersistenceStrategy(), SparqlBackendPersistenceStepFactory.class));
  }

}
