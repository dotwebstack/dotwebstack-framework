package org.dotwebstack.framework.service.openapi.helper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class JsonNodeUtils {

  private JsonNodeUtils() {}

  public static Object toObject(JsonNode node) {
    switch (node.getNodeType()) {
      case ARRAY:
        List<Object> result = new ArrayList<>();
        node.forEach(n -> result.add(toObject(n)));
        return result;
      case NUMBER:
      case BINARY:
        return node.asLong();
      case BOOLEAN:
        return node.asBoolean();
      case STRING:
        return node.asText();
      case OBJECT:
        Map<String, Object> map = new HashMap<>();
        node.fields()
            .forEachRemaining(entry -> map.put(entry.getKey(), toObject(entry.getValue())));
        return map;
      default:
        return null;

    }
  }
}
