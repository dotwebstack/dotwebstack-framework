package org.dotwebstack.framework.transaction.flow;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface FlowFactory {

  boolean supports(IRI flowType);

  Flow create(Model flowModel, IRI identifier);

}
