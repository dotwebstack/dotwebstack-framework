package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;

public interface BindableParameter<T> extends Parameter<T> {

  Literal getLiteral(@NonNull T value);

}
