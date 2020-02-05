package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Getter;

@Getter
public class RequestBodyContext {

  private RequestBody requestBodySchema;

  public RequestBodyContext(RequestBody requestBodySchema) {
    this.requestBodySchema = requestBodySchema;
  }
}
