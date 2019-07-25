package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Map;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private final ResponseContext openApiContext;

  private final GraphQL graphQL;

  private final GraphQlQueryBuilder queryBuilder;

  private final ObjectMapper objectMapper;

  CoreRequestHandler(ResponseContext openApiContext, GraphQL graphQL, GraphQlQueryBuilder queryBuilder,
      ObjectMapper objectMapper) {
    this.openApiContext = openApiContext;
    this.graphQL = graphQL;
    this.queryBuilder = queryBuilder;
    this.objectMapper = objectMapper;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    String mediaType = "application/hal+json";

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(buildQueryString())
        .build();

    try {
      ExecutionResult result = graphQL.execute(executionInput);
      if (result.getErrors()
          .isEmpty()) {
        ResponseObject template = openApiContext.getResponses()
            .stream()
            .filter(response -> response.isApplicable(200, 299, mediaType))
            .findFirst()
            .orElseThrow(
                () -> ExceptionHelper.unsupportedOperationException("MediaType '{}' was not found.", mediaType))
            .getResponseObject();

        String json = toJson(new ResponseMapper().mapResponse(template,
            ((Map<String, Object>) result.getData()).get(this.openApiContext.getGraphQlField()
                .getName())));
        return ServerResponse.ok()
            .body(fromObject(json));
      }
      return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(fromObject(String.format("GraphQl query resulted in errors: %s.", toJson(result.getErrors()))));
    } catch (JsonProcessingException e) {
      return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(fromObject("Error while serializing response to JSON."));
    }
  }


  private String toJson(Object object) throws JsonProcessingException {
    return objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(object);
  }

  private String buildQueryString() {
    return this.queryBuilder.toQuery(this.openApiContext.getGraphQlField());
  }
}
