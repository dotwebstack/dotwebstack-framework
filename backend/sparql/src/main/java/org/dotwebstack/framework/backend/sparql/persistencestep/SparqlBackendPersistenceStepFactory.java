package org.dotwebstack.framework.backend.sparql.persistencestep;

import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendPersistenceStepFactory {

  public PersistenceInsertIntoGraphStepExecutor create(PersistenceStep persistenceStep,
      Model transactionModel, SparqlBackend sparqlBackend) {
    if (persistenceStep.getPersistenceStrategy().equals(
        ELMO.PERSISTENCE_STRATEGY_INSERT_INTO_GRAPH)) {
      return new PersistenceInsertIntoGraphStepExecutor(persistenceStep, transactionModel,
          sparqlBackend);
    }

    throw new ConfigurationException(String.format("Strategy %s not support by %s",
        persistenceStep.getPersistenceStrategy(), SparqlBackendPersistenceStepFactory.class));
  }

}
