package org.dotwebstack.framework.service.openapi.exception;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.service.openapi.exception.ExceptionRuleHelper.getResponseStatus;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class OpenApiExceptionRouterHandler implements WebExceptionHandler {

  public static final String APPLICATION_PROBLEM_JSON_MIMETYPE = "application/problem+json";

  private final OpenAPI openApi;

  private final OpenApiDefaultExceptionHandler openApiDefaultExceptionHandler;

  private final OpenApiProblemExceptionHandler openApiProblemExceptionHandler;

  public OpenApiExceptionRouterHandler(final OpenAPI openApi, final OpenApiDefaultExceptionHandler openApiDefaultExceptionHandler,
                                       final OpenApiProblemExceptionHandler openApiProblemExceptionHandler) {
    this.openApi = openApi;
    this.openApiDefaultExceptionHandler = openApiDefaultExceptionHandler;
    this.openApiProblemExceptionHandler = openApiProblemExceptionHandler;
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
    Optional<HttpStatus> responseStatus = getResponseStatus(throwable);

    if (responseStatus.isPresent()) {
      ApiResponse apiResponse = getApiResponse(exchange, responseStatus.get());
      String contentType;

      if (apiResponse != null
          && APPLICATION_PROBLEM_JSON_MIMETYPE.equals((contentType = getContentType(apiResponse)))) {
        Schema<?> schema = resolveSchema(openApi, apiResponse.getContent()
            .get(contentType)
            .getSchema());
        return openApiProblemExceptionHandler.handle(schema, responseStatus.get(), exchange, throwable);
      }
    }

    return openApiDefaultExceptionHandler.handle(throwable);
  }

  private String getContentType(ApiResponse apiResponse) {
    return apiResponse.getContent()
        .keySet()
        .stream()
        .findFirst()
        .orElse("");
  }

  private ApiResponse getApiResponse(ServerWebExchange exchange, HttpStatus httpStatus) {
    HttpMethod httpMethod = getRequestHttpMethod(exchange.getRequest());

    Operation operation = getOasPath(httpMethod, exchange.getRequest()
        .getPath());

    String responseStatusKey = Integer.toString(httpStatus.value());

    return operation.getResponses()
        .get(responseStatusKey);
  }

  private HttpMethod getRequestHttpMethod(ServerHttpRequest request) {
    return ofNullable(request.getMethod()).orElseThrow();
  }

  public Operation getOasPath(HttpMethod httpMethod, RequestPath requestPath) {
    Optional<String> pathKey = openApi.getPaths()
        .keySet()
        .stream()
        .filter(key -> filter(key, requestPath.toString()))
        .max(Comparator.naturalOrder());

    return pathKey.map(key -> openApi.getPaths()
        .get(key))
        .map(pathItem -> pathItem.readOperationsMap()
            .get(PathItem.HttpMethod.valueOf(httpMethod.name())))
        .orElseThrow();
  }

  private boolean filter(String key, String requestPath) {
    String pattern = "\\{([a-zA-Z0-9]*)}";
    String keyPattern = key.replaceAll(pattern, "(.*)");
    keyPattern = keyPattern.replaceAll("\\/", "\\\\/");

    return requestPath.matches(keyPattern);
  }
}
