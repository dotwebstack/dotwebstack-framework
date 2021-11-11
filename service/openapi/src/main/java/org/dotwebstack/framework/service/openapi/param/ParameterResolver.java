package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.addEvaluatedDwsParameters;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.getParameterNamesOfType;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateParameterExistence;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateRequestBodyNonexistent;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@Slf4j
public class ParameterResolver {
  private RequestBody requestBody;

  private final RequestBodyContext requestBodyContext;

  private final ResponseSchemaContext responseSchemaContext;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final ParamHandlerRouter paramHandlerRouter;

  private final JexlHelper jexlHelper;

  private static final String REQUEST_URI = "request_uri";

  public ParameterResolver(RequestBody requestBody, RequestBodyContext requestBodyContext,
      ResponseSchemaContext responseSchemaContext, RequestBodyHandlerRouter requestBodyHandlerRouter,
      ParamHandlerRouter paramHandlerRouter, JexlEngine jexlEngine) {
    this.requestBody = requestBody;
    this.requestBodyContext = requestBodyContext;
    this.responseSchemaContext = responseSchemaContext;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.paramHandlerRouter = paramHandlerRouter;
    this.jexlHelper = new JexlHelper(jexlEngine);
  }

  public Mono<Map<String, Object>> resolveParameters(ServerRequest serverRequest) {
    var result = resolveUrlAndHeaderParameters(serverRequest);

    if (requestBody != null) {
      return requestBodyHandlerRouter.getRequestBodyHandler(requestBody)
          .getValues(serverRequest, requestBodyContext, requestBody, result)
          .map(values -> {
            result.putAll(values);
            return result;
          });
    }

    validateRequestBodyNonexistent(serverRequest);

    return Mono
        .just(addEvaluatedDwsParameters(result, responseSchemaContext.getDwsParameters(), serverRequest, jexlHelper));
  }

  Map<String, Object> resolveUrlAndHeaderParameters(ServerRequest request) {
    Map<String, Object> result = new HashMap<>();
    if (Objects.nonNull(this.responseSchemaContext.getParameters())) {
      result.put(REQUEST_URI, request.uri()
          .toString());

      validateParameterExistence("query", getParameterNamesOfType(this.responseSchemaContext.getParameters(), "query"),
          request.queryParams()
              .keySet());
      validateParameterExistence("path", getParameterNamesOfType(this.responseSchemaContext.getParameters(), "path"),
          request.pathVariables()
              .keySet());

      for (Parameter parameter : this.responseSchemaContext.getParameters()) {
        var handler = paramHandlerRouter.getParamHandler(parameter);
        handler.getValue(request, parameter, responseSchemaContext)
            .ifPresent(value -> result.put(handler.getParameterName(parameter), value));
      }
    }
    return result;
  }

}
