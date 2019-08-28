package org.dotwebstack.framework.service.openapi.requestbody;

import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.stereotype.Component;

@Component
public class RequestBodyHandlerRouter {
  private List<RequestBodyHandler> customHandlers;

  private RequestBodyHandler defaultHandler;

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private OpenAPI openApi;

  public RequestBodyHandlerRouter(List<RequestBodyHandler> customHandlers, OpenAPI openApi) {
    this.customHandlers = customHandlers;
    this.defaultHandler = new DefaultRequestBodyHandler(openApi, typeDefinitionRegistry);
    this.openApi = openApi;
  }

  public RequestBodyHandler getRequestBodyHandler(@NonNull RequestBodyContext requestBodyContext) {
    return this.customHandlers.stream()
        .filter(handler -> handler.supports(requestBodyContext))
        .findFirst()
        .orElse(this.defaultHandler);
  }
}
