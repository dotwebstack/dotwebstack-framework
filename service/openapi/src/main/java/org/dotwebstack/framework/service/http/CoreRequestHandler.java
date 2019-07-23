package org.dotwebstack.framework.service.http;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.FieldDefinition;
import java.util.Map;
import org.dotwebstack.framework.core.query.GraphQlQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private final GraphQL graphQL;

  private final FieldDefinition queryFieldDefinition;

  private final GraphQlQueryBuilder queryBuilder;

  private final ObjectMapper objectMapper;

  public CoreRequestHandler(GraphQL graphQL, FieldDefinition queryFieldDefinition, GraphQlQueryBuilder queryBuilder,
      ObjectMapper objectMapper) {
    this.graphQL = graphQL;
    this.queryFieldDefinition = queryFieldDefinition;
    this.queryBuilder = queryBuilder;
    this.objectMapper = objectMapper;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(buildQueryString())
        .build();

    try {
      ExecutionResult result = graphQL.execute(executionInput);
      if (result.getErrors()
          .isEmpty()) {
        String json = toJson(((Map<String, Object>) result.getData()).get(queryFieldDefinition.getName()));
        return ServerResponse.ok()
            .body(fromObject(json));
      }
      return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(fromObject(String.format("GraphQl query resulted in errors: %s", toJson(result.getErrors()))));
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
    return this.queryBuilder.toQuery(this.queryFieldDefinition);
  }
}
