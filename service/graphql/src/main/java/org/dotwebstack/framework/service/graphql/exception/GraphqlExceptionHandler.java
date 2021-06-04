package org.dotwebstack.framework.service.graphql.exception;

import static org.zalando.problem.Status.BAD_REQUEST;

import graphql.execution.UnknownOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.webflux.advice.ProblemHandling;
import reactor.core.publisher.Mono;

@Slf4j
@ControllerAdvice
public class GraphqlExceptionHandler implements ProblemHandling {

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<Problem>> handleIllegalArgumentException(Exception exception, ServerWebExchange request) {
    LOG.error(exception.getMessage());

    ThrowableProblem problem = Problem.builder()
        .withStatus(BAD_REQUEST)
        .withDetail(exception.getMessage())
        .build();

    return create(problem, request);
  }

  @ExceptionHandler(UnknownOperationException.class)
  public Mono<ResponseEntity<Problem>> handleUnknownOperationException(Exception exception, ServerWebExchange request) {
    LOG.error(exception.getMessage());

    ThrowableProblem problem = Problem.builder()
        .withStatus(BAD_REQUEST)
        .withDetail(exception.getMessage())
        .build();

    return create(problem, request);
  }
}
