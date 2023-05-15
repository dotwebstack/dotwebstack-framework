package org.dotwebstack.framework.backend.postgres.query;

import lombok.NonNull;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;

public class JsonMappingException extends DotWebStackRuntimeException {


  public JsonMappingException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }
}
