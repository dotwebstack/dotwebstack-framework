package org.dotwebstack.framework.service.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
class GraphqlController {

  private final GraphQL graphQL;

  @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  Mono<ExecutionResult> handleGet(@RequestParam("query") String query) {
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .build();

    return Mono.fromFuture(graphQL.executeAsync(executionInput));
  }

}
