package org.dotwebstack.framework.param;

import org.eclipse.rdf4j.model.Literal;

public interface BindableParameter<T> extends Parameter<T> {

  Literal toLiteral(T value);

}
