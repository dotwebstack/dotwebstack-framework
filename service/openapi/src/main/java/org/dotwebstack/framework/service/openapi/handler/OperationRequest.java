package org.dotwebstack.framework.service.openapi.handler;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class OperationRequest {

  @NonNull
  private final OperationContext context;

  @NonNull
  @Builder.Default
  private final Map<String, Object> parameters = Map.of();

  @NonNull
  private final String preferredMediaType;

  public Schema<?> getResponseSchema() {
    return context.getSuccessResponse()
        .getContent()
        .get(preferredMediaType)
        .getSchema();
  }
}
