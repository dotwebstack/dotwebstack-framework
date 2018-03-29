package org.dotwebstack.framework.transaction.flow.step.validation;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.transaction.flow.step.Step;
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
public class ValidationStepFactory implements StepFactory {

  private BackendResourceProvider backendResourceProvider;

  private ConfigurationBackend configurationBackend;

  @Autowired
  public ValidationStepFactory(@NonNull BackendResourceProvider backendResourceProvider,
      @NonNull ConfigurationBackend configurationBackend) {
    this.backendResourceProvider = backendResourceProvider;
    this.configurationBackend = configurationBackend;
  }

  @Override
  public boolean supports(IRI stepType) {
    return stepType.equals(ELMO.VALIDATION_STEP);
  }

  @Override
  public Step create(Model stepModel, Resource identifier) {
    ValidationStep.Builder builder =
        new ValidationStep.Builder(identifier, backendResourceProvider);
    getObjectString(stepModel, identifier, RDFS.LABEL).ifPresent(builder::label);
    getObjectIRI(stepModel, identifier, ELMO.CONFORMS_TO_PROP).ifPresent(builder::conformsTo);
    getObjectIRI(stepModel, identifier, ELMO.BACKEND_PROP).ifPresent(
        iri -> builder.backend(backendResourceProvider.get(iri)));
    builder.fileConfigurationBackend((FileConfigurationBackend) configurationBackend);

    return builder.build();
  }

  private Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

  private Optional<IRI> getObjectIRI(Model model, Resource subject, IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }

}
