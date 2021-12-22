package org.dotwebstack.framework.service.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.service.openapi.handler.OpenApiRequestHandler;
import org.dotwebstack.framework.service.openapi.handler.OperationHandlerFactory;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.server.HandlerFunction;
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

  private final InputStream openApiStream;

  private final OpenApiProperties openApiProperties;

  public RouterConfiguration(@NonNull OperationHandlerFactory operationHandlerFactory, @NonNull OpenAPI openApi,
      @NonNull InputStream openApiStream, @NonNull OpenApiProperties openApiProperties) {
    this.operationHandlerFactory = operationHandlerFactory;
    this.openApi = openApi;
    this.openApiStream = openApiStream;
    this.openApiProperties = openApiProperties;
  }

  @Bean
  public HttpAdviceTrait httpAdviceTrait() {
    return new HttpAdviceTrait() {};
  }

  @Bean
  @ConditionalOnProperty(prefix = "dotwebstack", name = "openapi.cors.enabled")
  public CorsWebFilter corsWebFilter() {
    var corsProperties = openApiProperties.getCors();

    var corsConfig = new CorsConfiguration();
    corsConfig.setAllowedMethods(List.of(HttpMethod.HEAD.name(), HttpMethod.GET.name(), HttpMethod.POST.name()));
    corsConfig.setAllowedHeaders(List.of(CorsConfiguration.ALL));
    corsConfig.setAllowedOriginPatterns(List.of(CorsConfiguration.ALL));
    corsConfig.setAllowCredentials(corsProperties.getAllowCredentials());
    corsConfig.setExposedHeaders(List.of(CorsConfiguration.ALL));
    corsConfig.setMaxAge(86400L);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }

  @Bean
  public RouterFunction<ServerResponse> router() {
    var builder = RouterFunctions.route();

    openApi.getPaths()
        .forEach((path, pathItem) -> builder.add(routePath(path, pathItem)));

    builder.add(routeApiDocs());
    return builder.build();
  }

  private RouterFunction<ServerResponse> routePath(String path, PathItem pathItem) {
    var builder = RouterFunctions.route();

    pathItem.readOperationsMap()
        .forEach((httpMethod, operation) -> {
          if (isDwsOperation(operation)) {
            builder.route(matchRoute(path, httpMethod), operationHandlerFactory.create(operation));
          }
        });

    builder.route(matchRoute(path, PathItem.HttpMethod.OPTIONS), optionsHandler(pathItem));

    return builder.build();
  }

  private boolean isDwsOperation(Operation operation) {
    return operation.getExtensions() != null && operation.getExtensions()
        .containsKey(OasConstants.X_DWS_QUERY) || operation.getResponses()
            .keySet()
            .stream()
            .map(Integer::parseInt)
            .map(HttpStatus::valueOf)
            .anyMatch(HttpStatus::is3xxRedirection);
  }

  private RouterFunction<ServerResponse> routeApiDocs() {
    return RouterFunctions.route(matchRoute(openApiProperties.getApiDocPublicationPath(), PathItem.HttpMethod.GET),
        new OpenApiRequestHandler(openApiStream));
  }

  private static RequestPredicate matchRoute(String path, PathItem.HttpMethod httpMethod) {
    var method = HttpMethod.valueOf(httpMethod.name());

    return RequestPredicates.path(path)
        .and(RequestPredicates.method(method));
  }

  private static HandlerFunction<ServerResponse> optionsHandler(PathItem pathItem) {
    var methodSet = new HashSet<>(Set.of(PathItem.HttpMethod.OPTIONS));

    methodSet.addAll(pathItem.readOperationsMap()
        .keySet());

    var allowMethods = methodSet.stream()
        .map(Enum::name)
        .collect(Collectors.joining(", "));

    var serverResponse = ServerResponse.noContent()
        .header(HttpHeaders.ALLOW, allowMethods)
        .build();

    return serverRequest -> serverResponse;
  }
}
