package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.addEvaluatedDwsParameters;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.getParameterNamesOfType;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateParameterExistence;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateRequestBodyNonexistent;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@Slf4j
public class DefaultParameterResolver implements ParameterResolver {
  private final RequestBody requestBody;

  private final RequestBodyContext requestBodyContext;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final ParamHandlerRouter paramHandlerRouter;

  private final JexlHelper jexlHelper;

  private static final String REQUEST_URI = "request_uri";

  private final List<Parameter> parameters;

  private final Map<String, String> dwsParameters;

  public DefaultParameterResolver(RequestBody requestBody, RequestBodyContext requestBodyContext,
      RequestBodyHandlerRouter requestBodyHandlerRouter, ParamHandlerRouter paramHandlerRouter, JexlEngine jexlEngine,
      List<Parameter> parameters, Map<String, String> dwsParameters) {
    this.requestBody = requestBody;
    this.requestBodyContext = requestBodyContext;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.paramHandlerRouter = paramHandlerRouter;
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.parameters = parameters;
    this.dwsParameters = dwsParameters;
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

    return Mono.just(addEvaluatedDwsParameters(result, dwsParameters, serverRequest, jexlHelper));
  }

  Map<String, Object> resolveUrlAndHeaderParameters(ServerRequest request) {
    Map<String, Object> result = new HashMap<>();

    if (Objects.nonNull(parameters)) {
      result.put(REQUEST_URI, request.uri()
          .toString());

      validateParameterExistence("query", getParameterNamesOfType(parameters, "query"), request.queryParams()
          .keySet());
      validateParameterExistence("path", getParameterNamesOfType(parameters, "path"), request.pathVariables()
          .keySet());

      for (Parameter parameter : parameters) {
        var handler = paramHandlerRouter.getParamHandler(parameter);
        handler.getValue(request, parameter)
            .ifPresent(value -> result.put(handler.getParameterName(parameter), value));
      }
    }

    return result;
  }
}
