package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.service.openapi.TestConstants.APPLICATION_JSON_HAL;
import static org.dotwebstack.framework.service.openapi.TestMocks.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

import graphql.ExecutionInput;
import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.schema.idl.errors.QueryOperationMissingError;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.dotwebstack.framework.core.InternalServerErrorException;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.NotAcceptableException;
import org.dotwebstack.framework.service.openapi.exception.NotFoundException;
import org.dotwebstack.framework.service.openapi.param.ParameterResolverFactory;
import org.dotwebstack.framework.service.openapi.query.QueryMapper;
import org.dotwebstack.framework.service.openapi.response.BodyMapper;
import org.dotwebstack.framework.service.openapi.response.header.ResponseHeaderResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OperationHandlerFactoryTest {

  private static OpenAPI openApi;

  @Mock
  private GraphQL graphQl;

  @Mock
  private QueryMapper queryMapper;

  @Mock
  private ParameterResolverFactory parameterResolverFactory;

  @Mock
  private ResponseHeaderResolver responseHeaderResolver;

  @Mock
  private BodyMapper bodyMapper;

  @BeforeAll
  static void beforeAll() {
    openApi = TestResources.openApi("openapi.yaml");
  }

  @Test
  void create_createsSucceedingHandler_ifConfigIsValid() {
    var operation = createOperation("/breweries", Map.of());
    when(bodyMapper.supports(any(), any())).thenReturn(true);
    when(bodyMapper.map(any(), any())).thenReturn(Mono.just(List.of()));

    var operationHandlerFactory = new OperationHandlerFactory(graphQl, queryMapper, List.of(bodyMapper),
        parameterResolverFactory, responseHeaderResolver);

    var executionInput = mock(ExecutionInput.class);
    var executionResult = TestResources.graphQlResult("brewery-collection");

    when(queryMapper.map(any())).thenReturn(executionInput);
    when(graphQl.executeAsync(executionInput)).thenReturn(CompletableFuture.completedFuture(executionResult));
    when(responseHeaderResolver.resolve(any(), any())).thenReturn(httpHeaders -> {
    });

    var result = operationHandlerFactory.create(operation);

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, "/breweries")))
        .assertNext(response -> {
          assertThat(response.statusCode(), is(HttpStatus.OK));
        })
        .verifyComplete();
  }

  @Test
  void create_createsRedirectingHandler_forValidConfigWithQuery() {
    var operation = createOperation("/brewery-old/{identifier}", Map.of("identifier", "foo"));

    var operationHandlerFactory = new OperationHandlerFactory(graphQl, queryMapper, List.of(bodyMapper),
        parameterResolverFactory, responseHeaderResolver);

    var executionInput = mock(ExecutionInput.class);
    var executionResult = TestResources.graphQlResult("brewery-old");

    when(queryMapper.map(any())).thenReturn(executionInput);
    when(graphQl.executeAsync(executionInput)).thenReturn(CompletableFuture.completedFuture(executionResult));
    when(responseHeaderResolver.resolve(any(), any())).thenReturn(httpHeaders -> {
    });

    var result = operationHandlerFactory.create(operation);

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, "/brewery-old/foo")))
        .assertNext(response -> {
          assertThat(response.statusCode(), is(HttpStatus.SEE_OTHER));
        })
        .verifyComplete();
  }

  @Test
  void create_createsRedirectingHandler_forValidConfigWithoutQuery() {
    var operation = createOperation("/brewery-old2/{identifier}", Map.of("identifier", "foo"));

    var operationHandlerFactory = new OperationHandlerFactory(graphQl, queryMapper, List.of(bodyMapper),
        parameterResolverFactory, responseHeaderResolver);

    when(responseHeaderResolver.resolve(any(), any())).thenReturn(httpHeaders -> {
    });

    var result = operationHandlerFactory.create(operation);

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, "/brewery-old2/foo")))
        .assertNext(response -> {
          assertThat(response.statusCode(), is(HttpStatus.SEE_OTHER));
        })
        .verifyComplete();
  }

  @Test
  void create_createsContentNegotiatingHandler_ifAcceptHeaderIsUsed() {
    var operation = createOperation("/breweries", Map.of());
    when(bodyMapper.supports(any(), any())).thenReturn(true);
    when(bodyMapper.map(any(), any())).thenReturn(Mono.just(Map.of()));

    var operationHandlerFactory = new OperationHandlerFactory(graphQl, queryMapper, List.of(bodyMapper),
        parameterResolverFactory, responseHeaderResolver);

    var executionInput = mock(ExecutionInput.class);
    var executionResult = TestResources.graphQlResult("brewery-collection");

    when(queryMapper.map(any())).thenReturn(executionInput);
    when(graphQl.executeAsync(executionInput)).thenReturn(CompletableFuture.completedFuture(executionResult));
    when(responseHeaderResolver.resolve(any(), any())).thenReturn(httpHeaders -> {
    });

    var result = operationHandlerFactory.create(operation);

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, APPLICATION_JSON, "/breweries")))
        .assertNext(response -> {
          assertThat(response.statusCode(), is(HttpStatus.OK));
          var responseHeaders = response.headers();
          assertThat(responseHeaders.getContentType(), is(APPLICATION_JSON));
        })
        .verifyComplete();

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, APPLICATION_JSON_HAL, "/breweries")))
        .assertNext(response -> {
          assertThat(response.statusCode(), is(HttpStatus.OK));
          var responseHeaders = response.headers();
          assertThat(responseHeaders.getContentType(), is(APPLICATION_JSON_HAL));
        })
        .verifyComplete();

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, APPLICATION_XML, "/breweries")))
        .verifyError(NotAcceptableException.class);
  }

  @Test
  void create_createsFailingHandler_ifResourceNotFound() {
    var operation = createOperation("/brewery/{identifier}", Map.of("identifier", "foo"));
    when(bodyMapper.supports(any(), any())).thenReturn(true);

    var operationHandlerFactory = new OperationHandlerFactory(graphQl, queryMapper, List.of(bodyMapper),
        parameterResolverFactory, responseHeaderResolver);

    var executionInput = mock(ExecutionInput.class);
    var executionResult = TestResources.graphQlResult("brewery-not-found");

    when(queryMapper.map(any())).thenReturn(executionInput);
    when(graphQl.executeAsync(executionInput)).thenReturn(CompletableFuture.completedFuture(executionResult));

    var result = operationHandlerFactory.create(operation);

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, "/brewery/foo")))
        .verifyError(NotFoundException.class);
  }

  @Test
  void create_createsFailingHandler_ifGraphQlErrorsOccur() {
    var operation = createOperation("/breweries", Map.of());
    when(bodyMapper.supports(any(), any())).thenReturn(true);

    var operationHandlerFactory = new OperationHandlerFactory(graphQl, queryMapper, List.of(bodyMapper),
        parameterResolverFactory, responseHeaderResolver);

    var executionInput = mock(ExecutionInput.class);
    var executionResult = ExecutionResultImpl.newExecutionResult()
        .addError(new QueryOperationMissingError())
        .build();

    when(queryMapper.map(any())).thenReturn(executionInput);
    when(graphQl.executeAsync(executionInput)).thenReturn(CompletableFuture.completedFuture(executionResult));

    var result = operationHandlerFactory.create(operation);

    StepVerifier.create(result.handle(mockRequest(HttpMethod.GET, "/breweries")))
        .verifyError(InternalServerErrorException.class);
  }

  @Test
  void create_throwsException_ifNoMatchingBodyMapperFound() {
    var operation = createOperation("/breweries", Map.of());

    when(bodyMapper.supports(any(), any())).thenReturn(true, false);

    var operationHandlerFactory = new OperationHandlerFactory(graphQl, queryMapper, List.of(bodyMapper),
        parameterResolverFactory, responseHeaderResolver);

    Assertions.assertThrows(InvalidConfigurationException.class, () -> operationHandlerFactory.create(operation));
  }

  private Operation createOperation(String path, Map<String, Object> parameters) {
    var operation = openApi.getPaths()
        .get(path)
        .getGet();

    when(parameterResolverFactory.create(operation)).thenReturn(serverRequest -> Mono.just(parameters));

    return operation;
  }
}
