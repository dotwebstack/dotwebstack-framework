package org.dotwebstack.framework.service.openapi.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OpenApiExceptionHandlerTest {

  @Mock
  private ObjectMapper mapper;

  @Mock
  private HttpAdviceTrait advice;

  @Mock
  private JexlEngine jexlEngine;

  @Mock
  private ServerWebExchange serverWebExchange;

  private final OpenAPI openApi = TestResources.openApi();

  private OpenApiExceptionHandler openApiExceptionHandler;

  @BeforeEach
  void setup() {
    this.openApiExceptionHandler = new OpenApiExceptionHandler(openApi, mapper, advice, jexlEngine);
  }

  @Test
  void handle_throwableProblem_returnsEntity() {
    // Arrange
    ThrowableProblem throwableProblem = mock(ThrowableProblem.class);
    Problem problem = Problem.builder()
        .build();
    ResponseEntity<Problem> responseEntity = ResponseEntity.badRequest()
        .body(problem);
    when(advice.create(throwableProblem, serverWebExchange)).thenReturn(Mono.just(responseEntity));

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwableProblem);

    // Assert
    verify(advice, times(1)).create(throwableProblem, serverWebExchange);
  }

  @Test
  void handle_responseStatusException_returnsEntity() {
    // Arrange
    ResponseStatusException throwable = mock(ResponseStatusException.class);
    Problem problem = Problem.builder()
        .build();
    ResponseEntity<Problem> responseEntity = ResponseEntity.badRequest()
        .body(problem);
    when(advice.create(throwable.getStatus(), throwable, serverWebExchange)).thenReturn(Mono.just(responseEntity));

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwable);

    // Assert
    verify(advice, times(1)).create(throwable.getStatus(), throwable, serverWebExchange);
  }

  @Test
  void handle_exceptionRule_returnsEntity() {
    // Arrange
    Throwable throwable = mock(ParameterValidationException.class);

    MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("/query3/123")
        .build();
    when(serverWebExchange.getRequest()).thenReturn(mockServerHttpRequest);

    Problem problem = Problem.builder()
        .build();
    ResponseEntity<Problem> responseEntity = ResponseEntity.badRequest()
        .body(problem);

    when(advice.create(eq(throwable), any(Problem.class), eq(serverWebExchange))).thenReturn(Mono.just(responseEntity));

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwable);

    // Assert
    verify(advice, times(1)).create(eq(throwable), any(Problem.class), eq(serverWebExchange));
  }
}
