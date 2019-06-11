package org.dotwebstack.framework.backend.rdf4j.helper;

import com.google.common.collect.ImmutableMap;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import java.util.HashMap;
import java.util.Map;

public class SparqlFilterHelper {

  private SparqlFilterHelper() {}

  @SuppressWarnings("unchecked")
  public static Map<String, Object> flatten(Map.Entry<String, Object> entry) {
    if (entry.getValue() instanceof Map) {
      return ((Map<String, Object>) entry.getValue()).entrySet()
          .stream()
          .flatMap(innerEntry -> flatten(innerEntry).entrySet()
              .stream())
          .collect(HashMap::new, (map, innerEntry) -> map.put(innerEntry.getKey(), innerEntry.getValue()),
              HashMap::putAll);
    }
    return ImmutableMap.of(entry.getKey(), entry.getValue());
  }

  public static Type<?> getBaseType(Type<?> type) {
    if (type instanceof ListType) {
      return getBaseType((Type<?>) type.getChildren()
          .get(0));
    }
    if (type instanceof NonNullType) {
      return getBaseType(((NonNullType) type).getType());
    }
    return type;
  }
}
