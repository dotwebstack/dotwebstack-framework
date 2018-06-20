package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;

abstract class AbstractEntity implements Entity {

  private final ApiResponse response;

  private final RequestContext requestContext;

  AbstractEntity(@NonNull ApiResponse response, @NonNull RequestContext requestContext) {
    this.response = response;
    this.requestContext = requestContext;
  }

  @Override
  public ApiResponse getResponse() {
    return response;
  }

  @Override
  public RequestContext getRequestContext() {
    return requestContext;
  }

}
