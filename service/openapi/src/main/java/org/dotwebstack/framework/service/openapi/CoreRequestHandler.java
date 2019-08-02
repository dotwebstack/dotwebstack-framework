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
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlQueryBuilder;
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

    Map<String, String> inputParams = resolveParametres(request);

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

  private Map<String, String> resolveParametres(ServerRequest request) {
    Map<String, String> result = new HashMap<>();
    this.responseContext.getParameters()
        .stream()
        .forEach(parameter -> resolveParameter(parameter, request, result));
    return result;
  }

  private void resolveParameter(Parameter parameter, ServerRequest request, Map<String, String> result) {
    switch (parameter.getIn()) {
      case "path":
        result.put(parameter.getName(), request.pathVariable(parameter.getName()));
        break;
      case "query":
        request.queryParam(parameter.getName())
            .ifPresent(value -> result.put(parameter.getName(), value));
        break;
      case "header":
        List<String> paramHeader = request.headers()
            .header(parameter.getName());
        if (!paramHeader.isEmpty()) {
          result.put(parameter.getName(), paramHeader.get(0));
        }
        break;
      default:
        throw ExceptionHelper.illegalArgumentException("Unsupported value for parameters.in: '{}'.", parameter.getIn());
    }
  }

  private String toJson(Object object) throws JsonProcessingException {
    return objectMapper.writer()
        .writeValueAsString(object);
  }

  private String buildQueryString(Map<String, String> inputParams) {
    return new GraphQlQueryBuilder().toQuery(this.responseContext.getGraphQlField(), inputParams);
  }
}
