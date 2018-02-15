package org.dotwebstack.framework.backend.sparql.persistencestep;

import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendPersistenceStepFactory {

  private final QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendPersistenceStepFactory(
      QueryEvaluator queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }

  public PersistenceInsertOrReplaceStepExecutor create(PersistenceStep persistenceStep,
      SparqlBackend sparqlBackend) {
    if (persistenceStep.getPersistenceStrategy().equals(ELMO.PERSISTENCE_STRATEGY_PROP)) {
      return new PersistenceInsertOrReplaceStepExecutor(persistenceStep, sparqlBackend);
    }

    throw new ConfigurationException(String.format("Strategy %s not support by backend %s",
        persistenceStep.getPersistenceStrategy(), sparqlBackend.getIdentifier()));
  }

}
