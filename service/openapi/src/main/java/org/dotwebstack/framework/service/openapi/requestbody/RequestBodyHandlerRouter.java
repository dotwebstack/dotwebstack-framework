package org.dotwebstack.framework.service.openapi.requestbody;

import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RequestBodyHandlerRouter {
  private List<RequestBodyHandler> customHandlers;

  private DefaultRequestBodyHandler defaultHandler;

  public RequestBodyHandlerRouter(List<RequestBodyHandler> handlers,
      @NonNull DefaultRequestBodyHandler defaultHandler) {
    this.customHandlers = handlers.stream()
        .filter(handler -> !Objects.equals(handler, defaultHandler))
        .collect(Collectors.toList());
    this.defaultHandler = defaultHandler;
  }

  public RequestBodyHandler getRequestBodyHandler(@NonNull RequestBody requestBody) {

    return this.customHandlers.stream()
        .filter(handler -> handler.supports(requestBody))
        .findFirst()
        .orElse(this.defaultHandler);
  }
}
