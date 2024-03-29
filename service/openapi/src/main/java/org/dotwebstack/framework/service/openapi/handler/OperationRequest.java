package org.dotwebstack.framework.service.openapi.handler;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;

@Builder
@Getter
public class OperationRequest {

  @NonNull
  private final OperationContext context;

  @NonNull
  @Builder.Default
  private final Map<String, Object> parameters = Map.of();

  private final MediaType preferredMediaType;

  @NonNull
  private final ServerRequest serverRequest;

  @SuppressWarnings("rawtypes")
  public Schema<?> getResponseSchema() {
    return context.getResponse()
        .getContent()
        .get(preferredMediaType.toString())
        .getSchema();
  }
}
