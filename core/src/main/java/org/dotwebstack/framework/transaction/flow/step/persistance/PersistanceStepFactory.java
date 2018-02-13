package org.dotwebstack.framework.transaction.flow.step.persistance;

import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.springframework.stereotype.Service;

@Service
public class PersistanceStepFactory implements StepFactory {

  @Override
  public boolean supports(IRI stepType) {
    return stepType == ELMO.VALIDATION_STEP;
  }

  @Override
  public PersistanceStep create(Model stepModel, IRI identifier) {
    return null;
  }
}
