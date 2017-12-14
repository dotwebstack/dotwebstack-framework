package org.dotwebstack.framework.frontend.openapi.entity;

import lombok.NonNull;

class EntityMapperRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 7089325450909214388L;

  public EntityMapperRuntimeException(@NonNull String message) {
    super(message);
  }

}
