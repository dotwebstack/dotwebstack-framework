package org.dotwebstack.framework.service.http;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

@Controller
class OpenApiService {

  private final GraphQL graphQL;

  public OpenApiService(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @RequestMapping(path = "/**", method = RequestMethod.GET)
  public Mono<ExecutionResult> get(ServerHttpRequest request) {
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .build();

    return Mono.fromFuture(graphQL.executeAsync(executionInput));
  }

}
