package org.dotwebstack.framework.service.openapi.exception;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OpenApiExceptionHandlerTest {

  @Mock
  private ObjectMapper mapper;

  @Mock
  private HttpAdviceTrait advice;

  @Spy
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
    when(throwableProblem.getStatus()).thenReturn(Status.BAD_REQUEST);

    when(advice.create(eq(throwableProblem), any(Problem.class), eq(serverWebExchange)))
        .thenAnswer(invocationOnMock -> {
          Problem problem = invocationOnMock.getArgument(1);
          return Mono.just(ResponseEntity.badRequest()
              .body(problem));
        });

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwableProblem);

    // Assert
    verify(advice, times(1)).create(eq(throwableProblem), any(Problem.class), eq(serverWebExchange));
  }

  @Test
  void handle_responseStatusException_returnsEntity() {
    // Arrange
    ResponseStatusException throwable = mock(ResponseStatusException.class);
    when(throwable.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);

    AtomicReference<ResponseEntity<Problem>> responseEntity = new AtomicReference<>();
    when(advice.create(throwable.getStatus(), throwable, serverWebExchange)).thenAnswer(invocationOnMock -> {
      Problem problem = Problem.builder()
          .build();
      responseEntity.set(ResponseEntity.status(throwable.getStatus())
          .body(problem));
      return Mono.just(responseEntity);
    });

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwable);

    // Assert
    verify(advice, times(1)).create(throwable.getStatus(), throwable, serverWebExchange);
    assertThat(responseEntity.get()
        .getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

  @Test
  void handle_exceptionRule_returnsEntity() {
    // Arrange
    Throwable throwable = mock(ParameterValidationException.class);

    MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("/query3/123")
        .build();
    when(serverWebExchange.getRequest()).thenReturn(mockServerHttpRequest);

    when(advice.create(eq(throwable), any(Problem.class), eq(serverWebExchange))).thenAnswer(invocationOnMock -> {
      Problem problem = invocationOnMock.getArgument(1);
      return Mono.just(ResponseEntity.badRequest()
          .body(problem));
    });

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwable);

    // Assert
    verify(advice, times(1)).create(eq(throwable), any(Problem.class), eq(serverWebExchange));
  }

  @Test
  void handle_exceptionRuleWithDwsExpression_returnsEntity() {
    // Arrange
    Throwable throwable = mock(NotFoundException.class);

    MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("/query3/123")
        .build();
    when(serverWebExchange.getRequest()).thenReturn(mockServerHttpRequest);

    JexlScript script = getJexlScript("`Not Found OAS`");
    when(jexlEngine.createScript("`Not Found OAS`")).thenReturn(script);

    AtomicReference<ResponseEntity<Problem>> responseEntity = new AtomicReference<>();
    when(advice.create(eq(throwable), any(Problem.class), eq(serverWebExchange))).thenAnswer(invocationOnMock -> {
      responseEntity.set(ResponseEntity.notFound()
          .build());
      return Mono.just(responseEntity);
    });

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwable);

    // Assert
    ArgumentCaptor<Problem> captor = ArgumentCaptor.forClass(Problem.class);
    verify(advice, times(1)).create(eq(throwable), captor.capture(), eq(serverWebExchange));

    List<Problem> actualProblem = captor.getAllValues();
    assertThat(actualProblem.get(0)
        .getTitle(), is(equalTo("Not Found OAS")));
    assertThat(actualProblem.get(0)
        .getStatus(), is(Status.NOT_FOUND));
  }

  @Test
  void handle_exceptionRuleWithAcceptableMimeTypes_returnsEntity() {
    // Arrange
    Throwable throwable = mock(NotAcceptableException.class);

    MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("/query3/123")
        .build();
    when(serverWebExchange.getRequest()).thenReturn(mockServerHttpRequest);

    JexlScript customParamScript = getJexlScript("`foo`");
    when(jexlEngine.createScript("`foo`")).thenReturn(customParamScript);

    JexlScript acceptableScript = getJexlScript("acceptableMimeTypes");
    when(jexlEngine.createScript("acceptableMimeTypes")).thenReturn(acceptableScript);

    AtomicReference<ResponseEntity<Problem>> responseEntity = new AtomicReference<>();
    when(advice.create(eq(throwable), any(Problem.class), eq(serverWebExchange))).thenAnswer(invocationOnMock -> {
      responseEntity.set(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
          .build());
      return Mono.just(responseEntity);
    });

    // Act
    openApiExceptionHandler.handle(serverWebExchange, throwable);

    // Assert
    ArgumentCaptor<Problem> captor = ArgumentCaptor.forClass(Problem.class);
    verify(advice, times(1)).create(eq(throwable), captor.capture(), eq(serverWebExchange));

    List<Problem> actualProblem = captor.getAllValues();
    assertThat(actualProblem.get(0)
        .getTitle(), is(equalTo("Unsupported media type requested")));
    assertThat(actualProblem.get(0)
        .getStatus(), is(Status.NOT_ACCEPTABLE));
    assertThat((String[]) actualProblem.get(0)
        .getParameters()
        .get("acceptable"), arrayContaining("application/hal+json"));
    assertThat(actualProblem.get(0)
        .getParameters()
        .get("customparam"), is("foo"));
  }

  // Because JexlEngine is mocked, .createScript returns null. This method returns a valid JexlScript.
  private JexlScript getJexlScript(String scriptText) {
    JexlEngine sjexl = new JexlBuilder().silent(false)
        .strict(true)
        .create();
    return sjexl.createScript(scriptText);
  }
}
