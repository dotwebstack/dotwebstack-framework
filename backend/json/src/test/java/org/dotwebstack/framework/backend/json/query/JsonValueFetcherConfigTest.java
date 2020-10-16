package org.dotwebstack.framework.backend.json.query;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.dotwebstack.framework.backend.json.converters.JsonConverterRouter;
import org.junit.jupiter.api.Test;

public class JsonValueFetcherConfigTest {

  private final JsonConverterRouter router = new JsonConverterRouter();

  private final JsonValueFetcherConfig jsonValueFetcherConfig = new JsonValueFetcherConfig();

  @Test
  void createJsonDefinitionTest() {
    assertDoesNotThrow(() -> jsonValueFetcherConfig.getValueFetcher(router));
  }
}
