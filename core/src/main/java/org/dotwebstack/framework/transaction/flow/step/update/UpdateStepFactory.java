package org.dotwebstack.framework.transaction.flow.step.update;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateStepFactory implements StepFactory {

  private BackendResourceProvider backendResourceProvider;

  @Autowired
  public UpdateStepFactory(@NonNull  BackendResourceProvider backendResourceProvider) {
    this.backendResourceProvider = backendResourceProvider;
  }

  @Override
  public boolean supports(@NonNull IRI stepType) {
    return stepType.equals(ELMO.UPDATE_STEP);
  }

  @Override
  public UpdateStep create(@NonNull Model stepModel, @NonNull Resource identifier) {
    UpdateStep.Builder builder =
        new UpdateStep.Builder(identifier, backendResourceProvider);
    getObjectString(stepModel, identifier, RDFS.LABEL).ifPresent(
        builder::label);
    getObjectString(stepModel, identifier, ELMO.QUERY).ifPresent(
        builder::query);
    getObjectIRI(stepModel, identifier, ELMO.BACKEND_PROP).ifPresent(
        builder::backend);

    return builder.build();
  }

  private Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

  private Optional<IRI> getObjectIRI(Model model, Resource subject, IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }

}
