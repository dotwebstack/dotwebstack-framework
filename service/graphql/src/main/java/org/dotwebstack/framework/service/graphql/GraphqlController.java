package org.dotwebstack.framework.service.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Map;
import org.dataloader.DataLoaderRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
class GraphqlController {

  private final GraphQL graphQL;

  public GraphqlController(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @CrossOrigin
  @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> handleGet(@RequestParam("query") String query) {
    var executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    return Mono.fromFuture(graphQL.executeAsync(executionInput))
        .map(ExecutionResult::toSpecification);
  }
}
