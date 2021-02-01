package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

public class MapHelper {

  private MapHelper() {}

  public static Map<String, Object> getNestedMap(Map<String, Object> arguments, String name) {
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      return castToMap(arguments.get(name));
    }
    return ImmutableMap.of();
  }

  public static Map<String, Object> toGraphQlMap(Map<String, Object> dataMap, Map<String, Object> fieldAliasMap) {
    Map<String, Object> result = new HashMap<>();
    for (String fieldName : fieldAliasMap.keySet()) {
      Object value = toGraphQlResultEntry(dataMap, fieldAliasMap.get(fieldName));

      result.put(fieldName, value);
    }

    return result;
  }

  private static Object toGraphQlResultEntry(Map<String, Object> dataMap, Object value) {
    if (value instanceof MapNode) {
      MapNode mapNode = (MapNode) value;

      return toGraphQlMap(dataMap, mapNode.getFieldAliasMap());
    }

    if (value instanceof String) {
      String alias = (String) value;

      if (dataMap.containsKey(alias) && dataMap.get(alias) != null) {
        return dataMap.get(alias);
      }
    }

    throw illegalStateException("Only Map or String instance is allowed as entry value");
  }
}
