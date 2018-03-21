package org.dotwebstack.framework.transaction.flow.step.assertion;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.springframework.stereotype.Service;

@Service
public class AssertionStepFactory implements StepFactory {

  @Override
  public boolean supports(@NonNull IRI stepType) {
    return stepType.equals(ELMO.ASSERTION_STEP);
  }

  @Override
  public AssertionStep create(@NonNull Model stepModel, @NonNull Resource identifier) {
    AssertionStep.Builder builder =
        new AssertionStep.Builder(identifier);
    getObjectString(stepModel, identifier, RDFS.LABEL).ifPresent(
        builder::label);
    builder.assertion(getObjectString(stepModel, identifier, ELMO.ASSERT),
        getObjectString(stepModel, identifier, ELMO.ASSERT_NOT));

    return builder.build();
  }

  private Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

}
