package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Getter;

@Getter
public class RequestBodyContext {

  private final RequestBody requestBodySchema;

  public RequestBodyContext(RequestBody requestBodySchema) {
    this.requestBodySchema = requestBodySchema;
  }
}
