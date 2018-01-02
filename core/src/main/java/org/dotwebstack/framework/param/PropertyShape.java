package org.dotwebstack.framework.param;

import org.eclipse.rdf4j.model.IRI;

public interface PropertyShape {

  IRI getDataType();

  Class<?> getTermClass();
}
