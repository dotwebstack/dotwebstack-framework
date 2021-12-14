package org.dotwebstack.framework.service.openapi;

import static org.dotwebstack.framework.service.openapi.TestMocks.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.service.openapi.handler.OpenApiRequestHandler;
import org.dotwebstack.framework.service.openapi.handler.OperationHandlerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RouterConfigurationTest {

  private static final OpenAPI openApi = TestResources.openApi("openapi-router.yaml");

  @Mock
  private OpenApiProperties openApiProperties;

  @Mock
  private OperationHandlerFactory operationHandlerFactory;

  private RouterConfiguration routerConfiguration;

  @BeforeEach
  void setUp() {
    routerConfiguration =
        new RouterConfiguration(operationHandlerFactory, openApi, TestResources.openApiStream(), openApiProperties);
  }

  @Test
  void httpAdviceTrait() {
    var httpAdviceTrait = routerConfiguration.httpAdviceTrait();
    assertThat(httpAdviceTrait, is(notNullValue()));
  }

  @Test
  void corsWebFilter_handlesGetRequest_whenOriginSent() {
    var corsProperties = new OpenApiProperties.CorsProperties();
    corsProperties.setEnabled(true);
    when(openApiProperties.getCors()).thenReturn(corsProperties);

    var exchange = mockExchange(HttpMethod.GET, Map.of(HttpHeaders.ORIGIN, "foo"));
    var chain = mock(WebFilterChain.class);
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    routerConfiguration.corsWebFilter()
        .filter(exchange, chain)
        .block();

    var responseHeaders = exchange.getResponse()
        .getHeaders();

    assertThat(responseHeaders.getAccessControlAllowOrigin(), is("foo"));
  }

  @Test
  void corsWebFilter_handlesPreflightRequest_whenOriginSent() {
    var corsProperties = new OpenApiProperties.CorsProperties();
    corsProperties.setEnabled(true);
    when(openApiProperties.getCors()).thenReturn(corsProperties);

    var exchange = mockExchange(HttpMethod.OPTIONS,
        Map.of(HttpHeaders.ORIGIN, "foo", HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()));

    routerConfiguration.corsWebFilter()
        .filter(exchange, mock(WebFilterChain.class))
        .block();

    var responseHeaders = exchange.getResponse()
        .getHeaders();

    assertThat(responseHeaders.getAccessControlAllowOrigin(), is("foo"));
    assertThat(responseHeaders.getAccessControlAllowHeaders(), empty());
    assertThat(responseHeaders.getAccessControlAllowCredentials(), is(false));
  }

  @Test
  void corsWebFilter_handlesPreflightRequest_whenOriginSentWithCredentials() {
    var corsProperties = new OpenApiProperties.CorsProperties();
    corsProperties.setEnabled(true);
    corsProperties.setAllowCredentials(true);
    when(openApiProperties.getCors()).thenReturn(corsProperties);

    var exchange = mockExchange(HttpMethod.OPTIONS,
        Map.of(HttpHeaders.ORIGIN, "foo", HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()));

    routerConfiguration.corsWebFilter()
        .filter(exchange, mock(WebFilterChain.class))
        .block();

    var responseHeaders = exchange.getResponse()
        .getHeaders();

    assertThat(responseHeaders.getAccessControlAllowOrigin(), is("foo"));
    assertThat(responseHeaders.getAccessControlAllowHeaders(), empty());
    assertThat(responseHeaders.getAccessControlAllowCredentials(), is(true));
  }

  @Test
  void corsWebFilter_handlesPreflightRequest_whenOriginSentWithHeaders() {
    var corsProperties = new OpenApiProperties.CorsProperties();
    corsProperties.setEnabled(true);
    when(openApiProperties.getCors()).thenReturn(corsProperties);

    var exchange =
        mockExchange(HttpMethod.OPTIONS, Map.of(HttpHeaders.ORIGIN, "foo", HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
            HttpMethod.GET.name(), HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type, X-Api-Key"));

    routerConfiguration.corsWebFilter()
        .filter(exchange, mock(WebFilterChain.class))
        .block();

    var responseHeaders = exchange.getResponse()
        .getHeaders();

    assertThat(responseHeaders.getAccessControlAllowOrigin(), is("foo"));
    assertThat(responseHeaders.getAccessControlAllowHeaders(), containsInAnyOrder("Content-Type", "X-Api-Key"));
  }

  private ServerWebExchange mockExchange(HttpMethod method, Map<String, String> headers) {
    var request = MockServerHttpRequest.method(method, "http://foo/bar");
    headers.forEach(request::header);

    var exchange = mock(ServerWebExchange.class);
    when(exchange.getRequest()).thenReturn(request.build());
    when(exchange.getResponse()).thenReturn(new MockServerHttpResponse());

    return exchange;
  }

  @Test
  void router_returnsHandler_whenPathMatches() {
    when(openApiProperties.getApiDocPublicationPath()).thenReturn("openapi.yaml");

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
  void router_returnsOptionsHandler_whenPathMatches() {
    when(openApiProperties.getApiDocPublicationPath()).thenReturn("openapi.yaml");
    when(operationHandlerFactory.create(any(Operation.class))).thenReturn(request -> Mono.empty());

    var routerFunction = routerConfiguration.router();
    var requestMock = mockRequest(HttpMethod.OPTIONS, "/breweries");

    StepVerifier.create(routerFunction.route(requestMock))
        .assertNext(handler -> assertOptionsHandler(handler, requestMock))
        .verifyComplete();
  }

  private void assertOptionsHandler(HandlerFunction<ServerResponse> optionsHandler, ServerRequest serverRequest) {
    StepVerifier.create(optionsHandler.handle(serverRequest))
        .assertNext(serverResponse -> {
          assertThat(serverResponse.statusCode(), is(HttpStatus.NO_CONTENT));
          var headers = serverResponse.headers();
          var expectedAllow = Set.of(HttpMethod.OPTIONS, HttpMethod.GET);
          assertThat(headers.getAllow(), is(equalTo(expectedAllow)));
        })
        .verifyComplete();
  }

  @Test
  void router_returnsNoHandler_whenPathNotFound() {
    when(openApiProperties.getApiDocPublicationPath()).thenReturn("openapi.yaml");

    when(operationHandlerFactory.create(any(Operation.class))).thenReturn(request -> Mono.empty());

    var routerFunction = routerConfiguration.router();

    verify(operationHandlerFactory, times(1)).create(any(Operation.class));

    StepVerifier.create(routerFunction.route(mockRequest(HttpMethod.GET, "/foo")))
        .verifyComplete();
  }

  @Test
  void router_createsApiDocHandler() {
    when(openApiProperties.getApiDocPublicationPath()).thenReturn("openapi.yaml");

    when(operationHandlerFactory.create(any(Operation.class))).thenReturn(request -> Mono.empty());

    var routerFunction = routerConfiguration.router();

    StepVerifier.create(routerFunction.route(mockRequest(HttpMethod.GET, "/openapi.yaml")))
        .assertNext(handler -> assertThat(handler, isA(OpenApiRequestHandler.class)))
        .verifyComplete();
  }

  @Test
  void router_returnsNoHandler_withoutXdwsQueryExtension() {
    when(openApiProperties.getApiDocPublicationPath()).thenReturn("openapi.yaml");

    when(operationHandlerFactory.create(any(Operation.class))).thenReturn(request -> Mono.empty());

    var routerFunction = routerConfiguration.router();

    StepVerifier.create(routerFunction.route(mockRequest(HttpMethod.GET, "/dws-operation-false")))
        .verifyComplete();
  }

}
