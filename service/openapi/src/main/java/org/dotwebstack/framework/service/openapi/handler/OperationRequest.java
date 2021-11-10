package org.dotwebstack.framework.service.openapi.handler;

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
}
