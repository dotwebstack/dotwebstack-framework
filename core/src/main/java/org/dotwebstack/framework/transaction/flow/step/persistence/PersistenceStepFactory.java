package org.dotwebstack.framework.transaction.flow.step.persistence;

import java.util.Optional;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.stereotype.Service;

@Service
public class PersistenceStepFactory implements StepFactory {

  private BackendResourceProvider backendResourceProvider;

  public PersistenceStepFactory(BackendResourceProvider backendResourceProvider) {
    this.backendResourceProvider = backendResourceProvider;
  }

  @Override
  public boolean supports(IRI stepType) {
    return stepType.equals(ELMO.PERSISTENCE_STEP);
  }

  @Override
  public PersistenceStep create(Model stepModel, IRI identifier) {
    PersistenceStep.Builder builder = new PersistenceStep.Builder(identifier);
    getObjectIRI(stepModel, identifier, ELMO.PERSISTENCE_STRATEGY_PROP).ifPresent(
        builder::persistenceStrategy);
    getObjectIRI(stepModel, identifier, ELMO.BACKEND_PROP).ifPresent(
        iri -> builder.backend(backendResourceProvider.get(iri)));
    getObjectIRI(stepModel, identifier, ELMO.TARGET_GRAPH_PROP).ifPresent(builder::targetGraph);

    return builder.build();
  }

  private Optional<IRI> getObjectIRI(Model model, IRI subject, IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }
}
