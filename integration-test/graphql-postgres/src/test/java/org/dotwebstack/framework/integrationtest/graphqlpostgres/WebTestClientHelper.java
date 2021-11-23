package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.jooq.tools.StringUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

class WebTestClientHelper {

  private static final String ERRORS = "errors";

  private static final String DATA = "data";

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static Map<String, Object> get(WebTestClient client, String query) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/");

    if (!StringUtils.isBlank(query)) {
      uriBuilder.queryParam("query", query);
    }

    if (!StringUtils.isBlank("")) {
      uriBuilder.queryParam("operationName", "");
    }

    if (!StringUtils.isBlank("")) {
      uriBuilder.queryParam("variables", "");
    }

    var result = client.get()
        .uri(uriBuilder.build()
            .toUri())
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    var mapResult = readMap(result);

    assertThat(mapResult.containsKey(ERRORS), is(false));
    assertThat(mapResult, hasKey(equalTo(DATA)));

    return getNestedMap(mapResult, DATA);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> readMap(String result) {
    try {
      return objectMapper.readValue(result, Map.class);
    } catch (JsonProcessingException exception) {
      throw illegalArgumentException(String.format("Failed to parse string to json: %s", result));
    }
  }
}
