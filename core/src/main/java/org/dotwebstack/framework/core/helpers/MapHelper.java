package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MapHelper {

  private MapHelper() {}

  public static Map<String, Object> getNestedMap(Map<String, Object> arguments, String name) {
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      Map<String, Object> map = castToMap(arguments.get(name));
      return resolveSuppliers(map);
    }
    return Map.of();
  }

  public static Map<String, Object> resolveSuppliers(Map<String, Object> map) {
    return map.entrySet()
        .stream()
        .collect(HashMap::new, (m, entry) -> m.put(entry.getKey(), resolveSuppliers(entry.getValue())),
            HashMap::putAll);
  }

  private static Object resolveSuppliers(Object value) {
    if (value instanceof Supplier) {
      return ((Supplier<?>) value).get();
    } else if (value instanceof Map) {
      Map<String, Object> map = castToMap(value);
      return resolveSuppliers(map);
    }

    return value;
  }
}
