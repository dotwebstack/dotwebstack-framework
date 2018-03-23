package org.dotwebstack.framework.transaction.flow.step.validation;

import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public class ValidationStepFactory implements StepFactory {

  @Override
  public boolean supports(IRI stepType) {
    return false;
  }

  @Override
  public Step create(Model flowModel, Resource identifier) {
    return null;
  }

}
