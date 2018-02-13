package org.dotwebstack.framework.transaction.flow.step.persistence;

import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.springframework.stereotype.Service;

@Service
public class PersistenceStepFactory implements StepFactory {

  @Override
  public boolean supports(IRI stepType) {
    return stepType == ELMO.VALIDATION_STEP;
  }

  @Override
  public PersistenceStep create(Model stepModel, IRI identifier) {
    return null;
  }
}
