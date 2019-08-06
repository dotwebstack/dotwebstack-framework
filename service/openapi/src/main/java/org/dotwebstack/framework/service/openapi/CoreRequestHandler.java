package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
    Map<String, Object> inputParams;

    try {
      inputParams = resolveParameters(request);
    } catch (ParameterValidationException e) {
      return ServerResponse.status(HttpStatus.NOT_FOUND)
          .body(fromObject(String.format("Error while obtaining request parameters: %s", e.getMessage())));
    }

    Map<String, Object> variables = inputParams.entrySet()
        .stream()
        .map(entry -> new AbstractMap.SimpleEntry<String, Object>(entry.getKey(), entry.getValue()))
        .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    String query = buildQueryString(inputParams);
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .variables(variables)
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

  private Map<String, Object> resolveParameters(ServerRequest request) throws ParameterValidationException {
    Map<String, Object> result = new HashMap<>();
    if (Objects.nonNull(this.responseContext.getParameters())) {
      for (Parameter parameter : this.responseContext.getParameters()) {
        resolveParameter(parameter, request, result);
      }
    }
    return result;
  }

  private void resolveParameter(Parameter parameter, ServerRequest request, Map<String, Object> result)
      throws ParameterValidationException {

    Object paramValue;
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

    Object deserialized = deserialize(parameter, paramValue);
    if (Objects.nonNull(deserialized)) {
      result.put(parameter.getName(), deserialized);
    }
  }

  private Object deserialize(Parameter parameter, Object paramValue) {

    String schemaType = parameter.getSchema()
        .getType();
    switch (schemaType) {
      case "array":
        return deserializeArray(parameter, paramValue);
      case "object":
        return deserializeObject(parameter, paramValue);
      default:
        return paramValue;
    }
  }

  private Object deserializeObject(Parameter parameter, Object paramValue) {
    return paramValue;
  }

  private Object deserializeArray(Parameter parameter, Object paramValue) {
    Parameter.StyleEnum style = parameter.getStyle();
    boolean explode = parameter.getExplode();

    if (style == Parameter.StyleEnum.SIMPLE && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(","));
    } else if (style == Parameter.StyleEnum.FORM && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(","));
    } else if (style == Parameter.StyleEnum.SPACEDELIMITED && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(" "));
    } else if (style == Parameter.StyleEnum.PIPEDELIMITED && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split("\\|"));
    } else {
      return paramValue;
    }
  }

  private Object getPathParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    try {
      return request.pathVariable(parameter.getName());
    } catch (Exception e) {
      if (parameter.getRequired()) {
        throw OpenApiExceptionHelper.parameterValidationException(
            "No value provided for required path parameter " + "'{}'.", parameter.getName());
      }
    }
    return null;
  }

  private Object getQueryParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    Object value = request.queryParams()
        .get(parameter.getName());

    if (parameter.getRequired() && Objects.isNull(value)) {
      throw OpenApiExceptionHelper.parameterValidationException("No value provided for required query parameter '{}'",
          parameter.getName());
    }
    return value;
  }

  private Object getHeaderParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    List<String> result = request.headers()
        .header(parameter.getName());
    if (result.isEmpty()) {
      if (parameter.getRequired()) {
        throw OpenApiExceptionHelper
            .parameterValidationException("No value provided for required header parameter '{}'", parameter.getName());
      }
    }
    if (!"array".equals(parameter.getSchema()
        .getType())) {
      return !result.isEmpty() ? result.get(0) : null;
    } else {
      return result;
    }
  }

  private String toJson(Object object) throws JsonProcessingException {
    return objectMapper.writer()
        .writeValueAsString(object);
  }

  private String buildQueryString(Map<String, Object> inputParams) {
    return new GraphQlQueryBuilder().toQuery(this.responseContext.getGraphQlField(), inputParams);
  }
}
