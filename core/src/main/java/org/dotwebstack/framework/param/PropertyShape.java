package org.dotwebstack.framework.param;

import org.eclipse.rdf4j.model.IRI;

public interface PropertyShape<T> {

  IRI getDataType();

  Class<?> getTermClass();

  T getDefaultValue();

}
