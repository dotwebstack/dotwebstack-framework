package org.dotwebstack.framework.service.openapi.exception;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.service.openapi.exception.ExceptionRuleHelper.getExceptionRule;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait;
import org.zalando.problem.spring.webflux.advice.utils.AdviceUtils;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class OpenApiExceptionHandler implements WebExceptionHandler {

  public static final String APPLICATION_PROBLEM_JSON_MIMETYPE = "application/problem+json";

  public static final String OAS_JSON_PROBLEM_TYPE = "type";

  public static final String OAS_JSON_PROBLEM_TITLE = "title";

  public static final String OAS_JSON_PROBLEM_DETAIL = "detail";

  public static final String OAS_JSON_PROBLEM_INSTANCE = "instance";

  public static final String OAS_JSON_PROBLEM_STATUS = "status";

  public static final List<String> OAS_JSON_PROBLEM_PROPERTIES = List.of(OAS_JSON_PROBLEM_INSTANCE,
      OAS_JSON_PROBLEM_TITLE, OAS_JSON_PROBLEM_TYPE, OAS_JSON_PROBLEM_DETAIL, OAS_JSON_PROBLEM_STATUS);

  private final OpenAPI openApi;

  private final ObjectMapper mapper;

  private final HttpAdviceTrait advice;

  private final JexlHelper jexlHelper;

  public OpenApiExceptionHandler(final OpenAPI openApi, final ObjectMapper objectMapper,
      final HttpAdviceTrait httpAdviceTrait, final JexlEngine jexlEngine) {
    this.openApi = openApi;
    this.mapper = objectMapper;
    this.advice = httpAdviceTrait;
    this.jexlHelper = new JexlHelper(jexlEngine);
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
    Mono<ResponseEntity<Problem>> responseEntity;

    if (throwable instanceof ResponseStatusException) {
      // In case endpoint doesn't exists
      ResponseStatusException responseStatusException = (ResponseStatusException) throwable;
      responseEntity = advice.create(responseStatusException.getStatus(), throwable, exchange);
    } else {
      Optional<Problem> problem;
      if (throwable instanceof ThrowableProblem) {
        problem = Optional.of((ThrowableProblem) throwable);
      } else {
        problem = getExceptionRule(throwable).map(rule -> toProblem(rule, throwable));
      }

      responseEntity = problem.map(p -> toCustomizedProblem(exchange, p))
          .map(p -> advice.create(throwable, p, exchange))
          .orElse(advice.create(throwable, exchange));
    }

    return responseEntity.flatMap(entity -> AdviceUtils.setHttpResponse(entity, exchange, mapper));
  }

  private Problem toProblem(ExceptionRule rule, Throwable throwable) {
    ProblemBuilder builder = Problem.builder()
        .withTitle(rule.getTitle())
        .withStatus(Optional.of(rule)
            .map(ExceptionRule::getResponseStatus)
            .map(HttpStatus::value)
            .map(Status::valueOf)
            .orElseThrow());

    if (rule.isDetail()) {
      builder.withDetail(throwable.getMessage());
    }

    return builder.build();
  }

  private Problem toCustomizedProblem(ServerWebExchange exchange, Problem problem) {
    ProblemBuilder builder = Problem.builder()
        .withDetail(problem.getDetail())
        .withTitle(problem.getTitle())
        .withInstance(problem.getInstance())
        .withType(problem.getType())
        .withStatus(problem.getStatus());

    problem.getParameters()
        .forEach(builder::with);

    HttpStatus responseStatus = ofNullable(problem.getStatus()).map(StatusType::getStatusCode)
        .map(HttpStatus::valueOf)
        .orElseThrow();

    MapContext mapContext = getMapContext(exchange);

    getSchema(exchange, responseStatus).ifPresent(schema -> {
      resolveExpressionUri(schema, mapContext, OAS_JSON_PROBLEM_TYPE).ifPresent(builder::withType);
      resolveExpression(schema, mapContext, OAS_JSON_PROBLEM_TITLE).map(Object::toString)
          .ifPresent(builder::withTitle);
      resolveExpression(schema, mapContext, OAS_JSON_PROBLEM_DETAIL).map(Object::toString)
          .ifPresent(builder::withDetail);
      resolveExpressionUri(schema, mapContext, OAS_JSON_PROBLEM_INSTANCE).ifPresent(builder::withInstance);

      schema.getProperties()
          .keySet()
          .stream()
          .filter(property -> !OAS_JSON_PROBLEM_PROPERTIES.contains(property))
          .forEach(property -> {
            if (schema.getProperties()
                .get(property)
                .getType()
                .equals("array")) {
              resolveExpressionArray(schema, mapContext, property).ifPresent(value -> builder.with(property, value));
            } else {
              resolveExpression(schema, mapContext, property).ifPresent(value -> builder.with(property, value));
            }
          });
    });

    return builder.build();
  }

  private MapContext getMapContext(ServerWebExchange exchange) {
    MapContext mapContext = new MapContext();

    String[] acceptableMimeTypes = getApiResponse(exchange, HttpStatus.OK).stream()
        .map(ApiResponse::getContent)
        .flatMap(content -> content.keySet()
            .stream())
        .toArray(String[]::new);

    mapContext.set("acceptableMimeTypes", acceptableMimeTypes);

    return mapContext;
  }

  @SuppressWarnings("rawtypes")
  private Optional<Schema<?>> getSchema(ServerWebExchange exchange, HttpStatus responseStatus) {
    return getApiResponse(exchange, responseStatus).map(ApiResponse::getContent)
        .map(content -> content.get(APPLICATION_PROBLEM_JSON_MIMETYPE))
        .map(MediaType::getSchema)
        .map(s -> resolveSchema(openApi, s));
  }

  @SuppressWarnings("rawtypes")
  private Optional<URI> resolveExpressionUri(Schema<?> schema, MapContext mapContext, String property) {
    return resolveExpression(schema, mapContext, property).map(Object::toString)
        .map(URI::create);
  }

  @SuppressWarnings("rawtypes")
  private Optional<Object> resolveExpression(Schema<?> schema, MapContext mapContext, String property) {
    return resolveScript(schema, property).flatMap(s -> jexlHelper.evaluateScript(s, mapContext, Object.class));
  }

  @SuppressWarnings("rawtypes")
  private Optional<Object[]> resolveExpressionArray(Schema<?> schema, MapContext mapContext, String property) {
    return resolveScript(schema, property).flatMap(s -> jexlHelper.evaluateScript(s, mapContext, Object[].class));
  }

  @SuppressWarnings("rawtypes")
  private Optional<String> resolveScript(Schema<?> schema, String property) {
    return Optional.of(schema)
        .map(Schema::getProperties)
        .map(map -> map.get(property))
        .filter(s -> s.getExtensions() != null && s.getExtensions()
            .containsKey(X_DWS_EXPR))
        .map(s -> s.getExtensions()
            .get(X_DWS_EXPR)
            .toString())
        .stream()
        .findFirst();
  }


  private Optional<ApiResponse> getApiResponse(ServerWebExchange exchange, HttpStatus responseStatus) {
    if (exchange == null || exchange.getRequest() == null) {
      return Optional.empty();
    }

    return getRequestHttpMethod(exchange.getRequest())
        .flatMap(httpMethod -> getPathOperation(httpMethod, exchange.getRequest()
            .getPath()))
        .map(Operation::getResponses)
        .map(apiResponses -> apiResponses.get(Integer.toString(responseStatus.value())));
  }

  private Optional<HttpMethod> getRequestHttpMethod(ServerHttpRequest request) {
    return ofNullable(request).map(HttpRequest::getMethod);
  }

  public Optional<Operation> getPathOperation(HttpMethod httpMethod, RequestPath requestPath) {
    Optional<String> pathKey = openApi.getPaths()
        .keySet()
        .stream()
        .filter(key -> filter(key, requestPath.toString()))
        .max(Comparator.naturalOrder());

    return pathKey.map(key -> openApi.getPaths()
        .get(key))
        .map(pathItem -> pathItem.readOperationsMap()
            .get(PathItem.HttpMethod.valueOf(httpMethod.name())));
  }

  private boolean filter(String key, String requestPath) {
    String keyPattern = key.replaceAll("\\{([a-zA-Z0-9_]*)}", "(.*)");
    keyPattern = keyPattern.replaceAll("\\/", "\\\\/");

    return requestPath.matches(keyPattern);
  }
}
