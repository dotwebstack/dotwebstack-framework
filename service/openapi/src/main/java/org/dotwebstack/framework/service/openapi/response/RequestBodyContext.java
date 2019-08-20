package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Getter;

@Getter
public class RequestBodyContext {
  private String name;

  private RequestBody requestBody;

  public RequestBodyContext(String name, RequestBody requestBody) {
    this.name = name;
    this.requestBody = requestBody;
  }
}
