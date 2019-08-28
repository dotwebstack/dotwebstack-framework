package org.dotwebstack.framework.service.openapi.requestbody;

import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.helper.SchemaResolver;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.stereotype.Component;

@Component
public class RequestBodyHandlerRouter {
  private List<RequestBodyHandler> customHandlers;

  private RequestBodyHandler defaultHandler;

  private OpenAPI openApi;

  public RequestBodyHandlerRouter(List<RequestBodyHandler> customHandlers, @NonNull OpenAPI openApi,
      @NonNull TypeDefinitionRegistry typeDefinitionRegistry) {
    this.customHandlers = customHandlers;
    this.openApi = openApi;
    this.defaultHandler = new DefaultRequestBodyHandler(openApi, typeDefinitionRegistry);
  }

  public RequestBodyHandler getRequestBodyHandler(@NonNull RequestBodyContext requestBodyContext) {
    RequestBody resolvedBody = SchemaResolver.resolveRequestBody(openApi, requestBodyContext.getRequestBodySchema());

    return this.customHandlers.stream()
        .filter(handler -> handler.supports(resolvedBody))
        .findFirst()
        .orElse(this.defaultHandler);
  }
}
