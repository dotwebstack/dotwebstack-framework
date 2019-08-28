package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Getter;

@Getter
public class RequestBodyContext {
  private String name;

  private RequestBody requestBodySchema;

  public RequestBodyContext(String name, RequestBody requestBodySchema) {
    this.name = name;
    this.requestBodySchema = requestBodySchema;
  }
}
