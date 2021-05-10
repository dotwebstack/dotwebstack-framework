package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import java.util.Map;

public class MapHelper {

  private MapHelper() {}

  public static Map<String, Object> getNestedMap(Map<String, Object> arguments, String name) {
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      return castToMap(arguments.get(name));
    }
    return Map.of();
  }
}
