package org.dotwebstack.framework.core;

import lombok.NonNull;

public class SchemaValidatorException extends DotWebStackRuntimeException {

  public SchemaValidatorException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }
}
