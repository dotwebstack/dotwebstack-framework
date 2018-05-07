package org.dotwebstack.framework.transaction.flow.step.persistence;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersistenceStepFactory implements StepFactory {

  private BackendResourceProvider backendResourceProvider;

  @Autowired
  public PersistenceStepFactory(@NonNull BackendResourceProvider backendResourceProvider) {
    this.backendResourceProvider = backendResourceProvider;
  }

  @Override
  public boolean supports(@NonNull IRI stepType) {
    return stepType.equals(ELMO.PERSISTENCE_STEP);
  }

  @Override
  public PersistenceStep create(@NonNull Model stepModel, @NonNull Resource identifier) {
    PersistenceStep.Builder builder =
        new PersistenceStep.Builder(identifier, backendResourceProvider);
    getObjectIRI(stepModel, identifier, ELMO.PERSISTENCE_STRATEGY_PROP).ifPresent(
        builder::persistenceStrategy);
    getObjectIRI(stepModel, identifier, ELMO.BACKEND_PROP).ifPresent(
        iri -> builder.backend(backendResourceProvider.get(iri)));
    getObjectIRI(stepModel, identifier, ELMO.TARGET_GRAPH_PROP).ifPresent(builder::targetGraph);

    return builder.build();
  }

  private Optional<IRI> getObjectIRI(@NonNull Model model, @NonNull Resource subject,
      @NonNull IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }

}
