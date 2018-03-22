package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.Response;
import lombok.NonNull;

abstract class AbstractEntity implements Entity {

  private Response response;

  AbstractEntity(@NonNull Response response) {
    this.response = response;
  }

  @Override
  public Response getResponse() {
    return response;
  }

}
