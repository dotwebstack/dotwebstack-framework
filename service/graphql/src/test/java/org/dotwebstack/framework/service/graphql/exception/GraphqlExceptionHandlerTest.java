package org.dotwebstack.framework.service.graphql.exception;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zalando.problem.Status.BAD_REQUEST;

import graphql.execution.UnknownOperationException;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.zalando.problem.Problem;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GraphqlExceptionHandlerTest {

  GraphqlExceptionHandler handler;

  @BeforeEach
  void init() {
    this.handler = new GraphqlExceptionHandler();
  }

  @Test
  void handleIllegalArgumentException_shouldReturnProblemJson_default() {
    IllegalArgumentException exception = illegalArgumentException("Illegal argument");

    ServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/?query=%7Bbeers%7Bidentifier_beer%20name%7D%7D")
            .build());

    Mono<ResponseEntity<Problem>> problem = handler.handleIllegalArgumentException(exception, exchange);

    StepVerifier.create(problem)
        .assertNext(result -> {
          assertThat(Objects.requireNonNull(result.getBody())
              .getStatus(), is(BAD_REQUEST));
          assertThat(result.getBody()
              .getDetail(), is("Illegal argument"));
        })
        .verifyComplete();
  }

  @Test
  void handleUnknownOperationException_shouldReturnProblemJson_default() {
    UnknownOperationException exception = new UnknownOperationException("Unknown operation");

    ServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/?query=%7Bbeers%7Bidentifier_beer%20name%7D%7D")
            .build());

    Mono<ResponseEntity<Problem>> problem = handler.handleUnknownOperationException(exception, exchange);

    StepVerifier.create(problem)
        .assertNext(result -> {
          assertThat(Objects.requireNonNull(result.getBody())
              .getStatus(), is(BAD_REQUEST));
          assertThat(result.getBody()
              .getDetail(), is("Unknown operation"));
        })
        .verifyComplete();
  }
}
