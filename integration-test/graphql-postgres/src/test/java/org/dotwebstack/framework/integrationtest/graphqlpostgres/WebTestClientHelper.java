package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.jooq.tools.StringUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;

class WebTestClientHelper {

  private static final String ERRORS = "errors";

  private static final String DATA = "data";

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static Map<String, Object> get(WebTestClient client, String query) {
    return get(client, query, "", Map.of());
  }

  public static Map<String, Object> get(WebTestClient client, String query, String operationName) {
    return get(client, query, operationName, Map.of());
  }

  public static Map<String, Object> get(WebTestClient client, String query, String operationName,
      Map<String, Object> variables) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/");

    if (!StringUtils.isBlank(query)) {
      uriBuilder.queryParam("query", query);
    }

    if (!StringUtils.isBlank(operationName)) {
      uriBuilder.queryParam("operationName", operationName);
    }

    if (!variables.isEmpty()) {
      JsonNode variableNode = objectMapper.convertValue(variables, JsonNode.class);
      uriBuilder.queryParam("variables", variableNode.toString());
    }

    var result = client.get()
        .uri(uriBuilder.build()
            .toUri())
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    return parseResult(result);
  }

  public static Map<String, Object> post(WebTestClient client, String body) {
    return post(client, body, "application/graphql");
  }

  public static Map<String, Object> post(WebTestClient client, String body, String contentType) {
    var result = client.post()
        .uri("/")
        .header("content-type", contentType)
        .body(BodyInserters.fromValue(body))
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    return parseResult(result);
  }

  private static Map<String, Object> parseResult(String result) {
    var mapResult = readMap(result);

    var error = mapResult.containsKey(ERRORS);

    if (!error && mapResult.containsKey(DATA)) {
      return getNestedMap(mapResult, DATA);
    }

    return mapResult;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> readMap(String result) {
    try {
      return objectMapper.readValue(result, Map.class);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException(String.format("Failed to parse string to json: %s", result));
    }
  }
}
