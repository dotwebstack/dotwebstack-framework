package org.dotwebstack.framework.service.openapi.exception;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.media.Schema;
import java.net.URI;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait;
import org.zalando.problem.spring.webflux.advice.utils.AdviceUtils;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OpenApiProblemExceptionHandler {

  public static final String OAS_JSON_PROBLEM_TYPE = "type";

  public static final String OAS_JSON_PROBLEM_TITLE = "title";

  public static final String OAS_JSON_PROBLEM_DETAIL = "detail";

  public static final String OAS_JSON_PROBLEM_INSTANCE = "instance";

  private final ObjectMapper mapper;

  private final HttpAdviceTrait advice;

  private final JexlHelper jexlHelper;

  public OpenApiProblemExceptionHandler(@NonNull final ObjectMapper mapper, @NonNull final HttpAdviceTrait advice,
      @NonNull final JexlEngine jexlEngine) {
    this.mapper = mapper;
    this.advice = advice;
    this.jexlHelper = new JexlHelper(jexlEngine);
  }

  public Mono<Void> handle(Schema<?> schema, HttpStatus httpStatus, ServerWebExchange exchange, Throwable throwable) {
    if (throwable instanceof ThrowableProblem) {
      final Mono<ResponseEntity<Problem>> entityMono = advice.create((ThrowableProblem) throwable, exchange);
      return entityMono.flatMap(entity -> AdviceUtils.setHttpResponse(entity, exchange, mapper));
    }

    return handleProblem(exchange, schema, httpStatus, throwable);
  }

  private Mono<Void> handleProblem(ServerWebExchange exchange, Schema<?> schema, HttpStatus httpStatus,
      Throwable throwable) {
    Problem problem = Problem.builder()
        .withType(resolveExpressionUri(schema, OAS_JSON_PROBLEM_TYPE))
        .withTitle(resolveExpression(schema, OAS_JSON_PROBLEM_TITLE))
        .withDetail(resolveExpression(schema, OAS_JSON_PROBLEM_DETAIL))
        .withInstance(resolveExpressionUri(schema, OAS_JSON_PROBLEM_INSTANCE))
        .withStatus(Status.valueOf(httpStatus.value()))
        .build();

    final Mono<ResponseEntity<Problem>> entityMono = advice.create(throwable, problem, exchange);
    return entityMono.flatMap(entity -> AdviceUtils.setHttpResponse(entity, exchange, mapper));
  }

  private URI resolveExpressionUri(Schema<?> schema, String property) {
    return ofNullable(resolveExpression(schema, property)).map(URI::create)
        .orElse(null);
  }

  private String resolveExpression(Schema<?> schema, String property) {
    if (!schema.getProperties()
        .containsKey(property)) {
      return null;
    }

    Optional<String> expr = Optional.of(schema.getProperties()
        .get(property))
        .filter(s -> s.getExtensions()
            .containsKey(X_DWS_EXPR))
        .map(s -> s.getExtensions()
            .get(X_DWS_EXPR)
            .toString())
        .stream()
        .findFirst();


    return expr.map(s -> jexlHelper.evaluateScript(s, new MapContext(), String.class)
        .orElseThrow())
        .orElse(null);
  }
}
