package org.dotwebstack.framework.service.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.service.openapi.handler.OperationHandlerFactory;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait;

@Configuration
@Slf4j
class RouterConfiguration {

  private final OperationHandlerFactory operationHandlerFactory;

  public RouterConfiguration(OperationHandlerFactory operationHandlerFactory) {
    this.operationHandlerFactory = operationHandlerFactory;
  }

  @Bean
  public HttpAdviceTrait httpAdviceTrait() {
    return new HttpAdviceTrait() {};
  }

  @Bean
  public RouterFunction<ServerResponse> router(@NonNull OpenAPI openApi) {
    var builder = RouterFunctions.route();

    openApi.getPaths()
        .forEach((path, pathItem) -> builder.add(routePath(path, pathItem)));

    return builder.build();
  }

  private RouterFunction<ServerResponse> routePath(String path, PathItem pathItem) {
    var builder = RouterFunctions.route();

    pathItem.readOperationsMap()
        .entrySet()
        .stream()
        .filter(entry -> isDwsRoute(entry.getValue()))
        .forEach(
            entry -> builder.route(matchRoute(path, entry.getKey()), operationHandlerFactory.create(entry.getValue())));

    return builder.build();
  }

  private RequestPredicate matchRoute(String path, PathItem.HttpMethod httpMethod) {
    var method = HttpMethod.valueOf(httpMethod.name());

    return RequestPredicates.path(path)
        .and(RequestPredicates.method(method));
  }

  private boolean isDwsRoute(Operation operation) {
    return operation.getExtensions()
        .containsKey(OasConstants.X_DWS_QUERY);
  }
}
