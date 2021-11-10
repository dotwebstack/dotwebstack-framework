package org.dotwebstack.framework.service.openapi.handler;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class OperationContext {

  @NonNull
  private final Operation operation;

  @NonNull
  private final ApiResponse successResponse;
}
