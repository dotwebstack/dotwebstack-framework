package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MapHelper {

  private MapHelper() {}

  public static Map<String, Object> getNestedMap(Map<String, Object> arguments, String name) {
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      return castToMap(arguments.get(name));
    }
    return ImmutableMap.of();
  }

  public static Map<String, Object> copyAndProcessSuppliers(Map<String, Object> map) {
    Map<String, Object> result = new HashMap<>();
    map.forEach((key, value) -> {
      if (value instanceof Supplier) {
        result.put(key, ((Supplier) value).get());
      } else if (value instanceof Map) {
        Map<String, Object> childMap = castToMap(value);
        result.put(key, copyAndProcessSuppliers(childMap));
      } else {
        result.put(key, value);
      }
    });
    return result;
  }
}
