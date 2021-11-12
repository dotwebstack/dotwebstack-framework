package org.dotwebstack.framework.service.openapi.param;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ParameterResolverFactory {

  private final JexlEngine jexlEngine;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final ParamHandlerRouter paramHandlerRouter;

  public ParameterResolverFactory(@NonNull JexlEngine jexlEngine,
      @NonNull RequestBodyHandlerRouter requestBodyHandlerRouter, @NonNull ParamHandlerRouter paramHandlerRouter) {
    this.jexlEngine = jexlEngine;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.paramHandlerRouter = paramHandlerRouter;
  }

  public ParameterResolver create(@NonNull Operation operation) {

    RequestBody requestBody = operation.getRequestBody();
    RequestBodyContext requestBodyContext = new RequestBodyContext(requestBody);

    List<Parameter> parameters =
        operation.getParameters() != null ? operation.getParameters() : Collections.emptyList();

    Map<String, String> dwsParameters = DwsExtensionHelper.getDwsQueryParameters(operation);

    return new ParameterResolver(requestBody, requestBodyContext, requestBodyHandlerRouter, paramHandlerRouter,
        jexlEngine, parameters, dwsParameters);
  }

}
