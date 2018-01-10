package org.dotwebstack.framework.param;

import org.eclipse.rdf4j.model.IRI;

public interface ParameterDefinition<T extends Parameter> {

  IRI getIdentifier();

  String getName();

  T createOptionalParameter();

  T createRequiredParameter();

}
