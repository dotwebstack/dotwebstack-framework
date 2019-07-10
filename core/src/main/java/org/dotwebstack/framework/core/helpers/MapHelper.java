package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class MapHelper {

  public static Map<String, Object> nestedMap(Map<String, Object> arguments, String name) {
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      return castToMap(arguments.get(name));
    }
    return ImmutableMap.of();
  }
}
