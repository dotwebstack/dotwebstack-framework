package org.dotwebstack.framework.service.openapi.param;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ParameterResolverFactory {

  private final OpenAPI openApi;

  private final JexlEngine jexlEngine;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final ParamHandlerRouter paramHandlerRouter;

  public ParameterResolverFactory(OpenAPI openApi, JexlEngine jexlEngine,
      RequestBodyHandlerRouter requestBodyHandlerRouter, ParamHandlerRouter paramHandlerRouter) {
    this.openApi = openApi;
    this.jexlEngine = jexlEngine;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.paramHandlerRouter = paramHandlerRouter;
  }

  public ParameterResolver create(@NonNull Operation operation) {
    // TODO: create

    RequestBody requestBody = operation.getRequestBody();
    RequestBodyContext requestBodyContext = new RequestBodyContext(requestBody);
    ResponseSchemaContext responseSchemaContext = null; // TODO: fill
    return new ParameterResolver(requestBody, requestBodyContext, responseSchemaContext, requestBodyHandlerRouter,
        paramHandlerRouter, jexlEngine);
  }

}
