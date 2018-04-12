package org.dotwebstack.framework.transaction.flow.step.validation;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationStepFactory implements StepFactory {

  private ConfigurationBackend configurationBackend;

  @Autowired
  public ValidationStepFactory(@NonNull ConfigurationBackend configurationBackend) {
    this.configurationBackend = configurationBackend;
  }

  @Override
  public boolean supports(IRI stepType) {
    return stepType.equals(ELMO.VALIDATION_STEP);
  }

  @Override
  public ValidationStep create(Model stepModel, Resource identifier) {
    final IRI conformsTo = getObjectIRI(stepModel, identifier, ELMO.CONFORMS_TO_PROP).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for validation step <%s>.",
                ELMO.CONFORMS_TO_PROP, identifier)));
    final Model validationModel;
    try {
      validationModel =
          QueryResults.asModel(configurationBackend.getRepository().getConnection().getStatements(
              null, null, null, conformsTo));
    } catch (RDF4JException ex) {
      throw new BackendException(
          String.format("Could not read configuration at graph {%s} as model {%s}", conformsTo,
              ex.getMessage()),
          ex);
    }
    ValidationStep.Builder builder = new ValidationStep.Builder(identifier, validationModel);
    builder.conformsTo(conformsTo);
    getObjectString(stepModel, identifier, RDFS.LABEL).ifPresent(builder::label);

    return builder.build();
  }

  private Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

  private Optional<IRI> getObjectIRI(Model model, Resource subject, IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }

}
