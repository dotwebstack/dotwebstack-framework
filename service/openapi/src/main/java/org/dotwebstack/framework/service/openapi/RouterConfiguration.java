package org.dotwebstack.framework.service.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.service.openapi.handler.OperationHandlerFactory;
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

  private final OpenAPI openApi;

  public RouterConfiguration(@NonNull OperationHandlerFactory operationHandlerFactory, @NonNull OpenAPI openApi) {
    this.operationHandlerFactory = operationHandlerFactory;
    this.openApi = openApi;
  }

  @Bean
  public HttpAdviceTrait httpAdviceTrait() {
    return new HttpAdviceTrait() {};
  }

  @Bean
  public RouterFunction<ServerResponse> router() {
    var builder = RouterFunctions.route();

    openApi.getPaths()
        .forEach((path, pathItem) -> builder.add(routePath(path, pathItem)));

    return builder.build();
  }

  private RouterFunction<ServerResponse> routePath(String path, PathItem pathItem) {
    var builder = RouterFunctions.route();

    pathItem.readOperationsMap()
        .forEach((httpMethod, operation) -> builder.route(matchRoute(path, httpMethod),
            operationHandlerFactory.create(operation)));

    return builder.build();
  }

  private static RequestPredicate matchRoute(String path, PathItem.HttpMethod httpMethod) {
    var method = HttpMethod.valueOf(httpMethod.name());

    return RequestPredicates.path(path)
        .and(RequestPredicates.method(method));
  }
}
