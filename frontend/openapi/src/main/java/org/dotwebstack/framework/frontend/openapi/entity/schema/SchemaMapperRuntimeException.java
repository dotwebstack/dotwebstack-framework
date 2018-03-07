package org.dotwebstack.framework.frontend.openapi.entity.schema;

import lombok.NonNull;

public class SchemaMapperRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -3200657648895745521L;

  public SchemaMapperRuntimeException(@NonNull String message) {
    super(message);
  }

  public SchemaMapperRuntimeException(@NonNull String message, @NonNull Exception cause) {
    super(message, cause);
  }

  public SchemaMapperRuntimeException(@NonNull Exception cause) {
    super(cause);
  }

}
