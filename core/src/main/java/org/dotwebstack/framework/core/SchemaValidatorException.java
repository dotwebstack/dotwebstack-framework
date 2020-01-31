package org.dotwebstack.framework.core;

import lombok.NonNull;

public class SchemaValidatorException extends DotWebStackRuntimeException {

  private static final long serialVersionUID = 7760665084567935669L;

  public SchemaValidatorException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }
}
