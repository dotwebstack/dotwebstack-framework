package org.dotwebstack.framework.core.traversers;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

class TraverserHelper {

  private TraverserHelper() {}

  static Map<String, Object> flattenArguments(Map<String, Object> arguments) {
    return arguments.entrySet()
        .stream()
        .flatMap(entry -> TraverserHelper.flatten(entry)
            .entrySet()
            .stream())
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> flatten(Map.Entry<String, Object> entry) {
    if (entry.getValue() instanceof Map) {
      return ((Map<String, Object>) entry.getValue()).entrySet()
          .stream()
          .flatMap(innerEntry -> flatten(innerEntry).entrySet().stream())
          .collect(HashMap::new, (map, innerEntry) -> map.put(innerEntry.getKey(), innerEntry.getValue()),
              HashMap::putAll);
    }
    return ImmutableMap.of(entry.getKey(), entry.getValue());
  }
}
