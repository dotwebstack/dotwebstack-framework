package org.dotwebstack.framework.service.openapi.jexl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class JsonFunctionsTest {

  @Test
  public void toJson_returnsNull_forNullInput() {
    assertNull(new JsonFunctions().asString(null));
  }

  @Test
  public void toJson_returnsExpectedValue() {
    Map<String, Object> input = new TreeMap<>();
    input.put("key1", List.of("v1", "v2"));
    input.put("key2", 2);
    String json = new JsonFunctions().asString(input);

    assertThat(json, is("{\"key1\":[\"v1\",\"v2\"],\"key2\":2}"));
  }

}
