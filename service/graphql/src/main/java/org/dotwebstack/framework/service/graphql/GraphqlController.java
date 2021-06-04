package org.dotwebstack.framework.service.graphql;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoaderRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
class GraphqlController {

  private final GraphQL graphQL;

  private final ObjectMapper objectMapper;

  private static final String QUERY = "query";

  private static final String OPERATION_NAME = "operationName";

  private static final String VARIABLES = "variables";

  public GraphqlController(GraphQL graphQL) {
    this.graphQL = graphQL;
    this.objectMapper = new ObjectMapper();
  }

  @CrossOrigin
  @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> handleGet(@RequestParam(QUERY) String query,
      @RequestParam(value = OPERATION_NAME, required = false) String operationName,
      @RequestParam(value = VARIABLES, required = false) String variablesJson) {

    if (query.contains("$")) {
      var parameterName = query.substring(query.indexOf('$') + 1, query.indexOf(":"));
      if (!variablesJson.contains(parameterName)) {
        throw illegalArgumentException(String.format("Missing value '%s' in variables '%s', for parameter '%s'.",
            parameterName, variablesJson, parameterName));
      }
    }

    Map<String, Object> variablesMap = convertVariablesJson(variablesJson);

    var executionInput = getExecutionInput(query, operationName, variablesMap);

    return Mono.fromFuture(graphQL.executeAsync(executionInput))
        .map(ExecutionResult::toSpecification);
  }

  @CrossOrigin
  @PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> handlePost(@RequestBody Map<String, Object> requestBody) {

    var executionInput = getExecutionInput(requestBody);

    return Mono.fromFuture(graphQL.executeAsync(executionInput))
        .map(ExecutionResult::toSpecification);
  }

  @CrossOrigin
  @PostMapping(value = "/graphql", consumes = "application/graphql", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> handlePost(@RequestBody(required = false) String body) {

    var executionInput = getExecutionInput(body, null, Map.of());

    return Mono.fromFuture(graphQL.executeAsync(executionInput))
        .map(ExecutionResult::toSpecification);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> convertVariablesJson(String jsonMap) {
    if (jsonMap == null) {
      return Map.of();
    }

    try {
      return objectMapper.readValue(jsonMap, Map.class);
    } catch (IOException e) {
      throw illegalArgumentException("Could not convert variables GET parameter: expected a JSON map");
    }
  }

  private ExecutionInput getExecutionInput(Map<String, Object> requestBody) {
    return getExecutionInput((String) requestBody.get(QUERY), (String) requestBody.get(OPERATION_NAME),
        getNestedMap(requestBody, VARIABLES));
  }

  private ExecutionInput getExecutionInput(String query, String operationName, Map<String, Object> variables) {
    return ExecutionInput.newExecutionInput()
        .query(query)
        .operationName(operationName)
        .variables(variables)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();
  }
}
