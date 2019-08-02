package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private final ResponseContext responseContext;

  private final GraphQL graphQL;

  private final ObjectMapper objectMapper;

  CoreRequestHandler(ResponseContext responseContext, GraphQL graphQL, ObjectMapper objectMapper) {
    this.responseContext = responseContext;
    this.graphQL = graphQL;
    this.objectMapper = objectMapper;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    Map<String, String> inputParams;

    try {
      inputParams = resolveParameters(request);
    } catch (ParameterValidationException e) {
      return ServerResponse.status(HttpStatus.NOT_FOUND)
          .body(fromObject(String.format("Error while obtaining request parameters: %s", e.getMessage())));
    }

    String query = buildQueryString(inputParams);
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .build();

    try {
      ExecutionResult result = graphQL.execute(executionInput);
      if (result.getErrors()
          .isEmpty()) {
        ResponseTemplate template = responseContext.getResponses()
            .stream()
            .filter(response -> response.isApplicable(200, 299))
            .findFirst()
            .orElseThrow(
                () -> ExceptionHelper.unsupportedOperationException("No response found within the 200 range."));

        Object response = new ResponseMapper().mapResponse(template.getResponseObject(),
            ((Map<String, Object>) result.getData()).get(this.responseContext.getGraphQlField()
                .getName()));
        if (response == null) {
          return ServerResponse.notFound()
              .build();
        }
        String json = toJson(response);
        return ServerResponse.ok()
            .contentType(MediaType.parseMediaType(template.getMediaType()))
            .body(fromObject(json));
      }
      return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(fromObject(String.format("GraphQl query resulted in errors: %s.", toJson(result.getErrors()))));
    } catch (JsonProcessingException e) {
      return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(fromObject("Error while serializing response to JSON."));
    }
  }

  private Map<String, String> resolveParameters(ServerRequest request) throws ParameterValidationException {
    Map<String, String> result = new HashMap<>();
    for (Parameter parameter : this.responseContext.getParameters()) {
      resolveParameter(parameter, request, result);
    }
    return result;
  }

  private void resolveParameter(Parameter parameter, ServerRequest request, Map<String, String> result)
      throws ParameterValidationException {
    String paramValue;
    switch (parameter.getIn()) {
      case "path":
        paramValue = getPathParam(parameter, request);
        break;
      case "query":
        paramValue = getQueryParam(parameter, request);
        break;
      case "header":
        paramValue = getHeaderParam(parameter, request);
        break;
      default:
        throw ExceptionHelper.illegalArgumentException("Unsupported value for parameters.in: '{}'.", parameter.getIn());
    }

    if (Objects.nonNull(paramValue)) {
      result.put(parameter.getName(), paramValue);
    }
  }

  private String getPathParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    try {
      return request.pathVariable(parameter.getName());
    } catch (Exception e) {
      if (parameter.getRequired()) {
        throw OpenApiExceptionHelper.parameterValidationException("No value provided for required path parameter '{}'",
            parameter.getName());
      }
    }
    return null;
  }

  private String getQueryParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    String param = request.queryParam(parameter.getName())
        .orElse(null);

    if (parameter.getRequired() && Objects.isNull(param)) {
      throw OpenApiExceptionHelper.parameterValidationException("No value provided for required query parameter '{}'",
          parameter.getName());
    }
    return param;
  }

  private String getHeaderParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    List<String> paramHeader = request.headers()
        .header(parameter.getName());
    if (paramHeader.isEmpty()) {
      if (parameter.getRequired()) {
        throw OpenApiExceptionHelper
            .parameterValidationException("No value provided for required header parameter '{}'", parameter.getName());
      }
      return null;
    }
    return paramHeader.get(0);
  }

  private String toJson(Object object) throws JsonProcessingException {
    return objectMapper.writer()
        .writeValueAsString(object);
  }

  private String buildQueryString(Map<String, String> inputParams) {
    return new GraphQlQueryBuilder().toQuery(this.responseContext.getGraphQlField(), inputParams);
  }
}
