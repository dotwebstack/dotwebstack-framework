package org.dotwebstack.framework.param;

import org.eclipse.rdf4j.model.Literal;

public interface BindableParameter extends Parameter {

  Literal toLiteral(Object value);

}
