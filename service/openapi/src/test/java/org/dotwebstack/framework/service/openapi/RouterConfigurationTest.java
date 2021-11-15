package org.dotwebstack.framework.service.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.util.ResolverFully;
import java.util.List;
import org.dotwebstack.framework.core.ResourceProperties;
import org.dotwebstack.framework.service.openapi.handler.OperationHandlerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RouterConfigurationTest {

  private static OpenAPI openApi;

  @Mock
  private OperationHandlerFactory operationHandlerFactory;

  private RouterConfiguration routerConfiguration;

  @BeforeAll
  static void beforeAll() {
    openApi = new OpenAPIV3Parser().read(ResourceProperties.getResourcePath()
        .resolve("openapi-router.yaml")
        .getPath());

    new ResolverFully().resolveFully(openApi);
  }

  @BeforeEach
  void setUp() {
    routerConfiguration = new RouterConfiguration(operationHandlerFactory, openApi);
  }

  @Test
  void httpAdviceTrait() {
    var httpAdviceTrait = routerConfiguration.httpAdviceTrait();
    assertThat(httpAdviceTrait, is(notNullValue()));
  }

  @Test
  void router_returnsHandler_whenPathMatches() {
    HandlerFunction<ServerResponse> operationHandler = request -> ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue("[]"));

    when(operationHandlerFactory.create(any(Operation.class))).thenReturn(operationHandler);

    var routerFunction = routerConfiguration.router();

    verify(operationHandlerFactory, times(1)).create(any(Operation.class));

    StepVerifier.create(routerFunction.route(mockRequest(HttpMethod.GET, "/breweries")))
        .assertNext(handler -> assertThat(handler, is(operationHandler)))
        .verifyComplete();
  }

  @Test
  void router_returnsNoHandler_whenPathNotFound() {
    when(operationHandlerFactory.create(any(Operation.class))).thenReturn(request -> Mono.empty());

    var routerFunction = routerConfiguration.router();

    verify(operationHandlerFactory, times(1)).create(any(Operation.class));

    StepVerifier.create(routerFunction.route(mockRequest(HttpMethod.GET, "/foo")))
        .verifyComplete();
  }

  private ServerRequest mockRequest(HttpMethod httpMethod, String path) {
    var serverHttpRequest = MockServerHttpRequest.method(httpMethod, path)
        .build();

    return ServerRequest.create(MockServerWebExchange.from(serverHttpRequest), List.of());
  }
}
