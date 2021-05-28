package org.dotwebstack.framework.service.openapi;

import io.swagger.v3.oas.models.Operation;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;

@Builder
@Getter
public class HttpMethodOperation {
  private final String name;

  private final HttpMethod httpMethod;

  private final Operation operation;
}
