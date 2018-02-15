package org.dotwebstack.framework.transaction.flow.step;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface StepFactory {

  boolean supports(IRI stepType);

  Step create(Model flowModel, Resource identifier);

}
