package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

public interface BindableParameter<T> extends Parameter<T> {

  Value getValue(@NonNull T value);

}
