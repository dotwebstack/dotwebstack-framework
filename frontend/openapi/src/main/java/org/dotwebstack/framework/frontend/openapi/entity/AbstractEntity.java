package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.Response;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;

abstract class AbstractEntity implements Entity {

  private final Response response;

  private final RequestContext requestContext;

  AbstractEntity(@NonNull Response response, @NonNull RequestContext requestContext) {
    this.response = response;
    this.requestContext = requestContext;
  }

  @Override
  public Response getResponse() {
    return response;
  }

  @Override
  public RequestContext getRequestContext() {
    return requestContext;
  }

}
