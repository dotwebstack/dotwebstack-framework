package org.dotwebstack.framework.service.openapi.handler;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.MediaType;

@Builder
@Getter
public class OperationRequest {

  @NonNull
  private final OperationContext context;

  @NonNull
  @Builder.Default
  private final Map<String, Object> parameters = Map.of();

  @NonNull
  private final MediaType preferredMediaType;

  @SuppressWarnings("rawtypes")
  public Schema<?> getResponseSchema() {
    return context.getSuccessResponse()
        .getContent()
        .get(preferredMediaType.toString())
        .getSchema();
  }
}
