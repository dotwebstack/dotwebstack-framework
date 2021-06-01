package org.dotwebstack.framework.service.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Map;
import org.dataloader.DataLoaderRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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


//  @CrossOrigin
//  @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
//  public Mono<Map<String, Object>> handleGet(@RequestParam("query") String query,
//                                             @RequestParam(value = "operationName", required = false) String operationName,
//                                             @RequestParam("variables") Map<String, Object> variables) {
//
//    var executionInput = getExecutionInput(query, operationName, variables);
//
//    return Mono.fromFuture(graphQL.executeAsync(executionInput))
//        .map(ExecutionResult::toSpecification);
//  }


  @CrossOrigin
  @PostMapping(value="/graphql", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> handlePost(@RequestBody Map<String,Object> requestBody) {

    var executionInput = getExecutionInput(requestBody);

    return Mono.fromFuture(graphQL.executeAsync(executionInput))
        .map(ExecutionResult::toSpecification);
  }

  @SuppressWarnings("unchecked")
  private ExecutionInput getExecutionInput(Map<String,Object> requestBody) {
    return getExecutionInput((String) requestBody.get("query"), (String) requestBody.get("operationName"), (Map<String, Object>) requestBody.get("variables"));
  }

  private ExecutionInput getExecutionInput(String query, String operationName, Map<String, Object> variables) {


    var input = ExecutionInput.newExecutionInput()
        .query(query)
        .operationName(operationName)
        .variables(variables)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    return input;
  }
}
