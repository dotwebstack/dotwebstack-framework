package org.dotwebstack.framework.backend.rdf4j.mapping;

import lombok.NonNull;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;

class ResponseMapperException extends DotWebStackRuntimeException {

  static final long serialVersionUID = 1563735990022L;

  ResponseMapperException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }
}
