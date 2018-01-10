package org.dotwebstack.framework.frontend.openapi.entity.schema;

import lombok.NonNull;

class SchemaMapperRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -3200657648895745521L;

  public SchemaMapperRuntimeException(@NonNull String message) {
    super(message);
  }

  public SchemaMapperRuntimeException(String message, Exception ex) {
    super(message, ex);
  }

}
