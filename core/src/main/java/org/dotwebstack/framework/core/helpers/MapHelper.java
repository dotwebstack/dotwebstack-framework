package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import java.util.Map;
import java.util.function.Supplier;

public class MapHelper {

  private MapHelper() {}

  public static Map<String, Object> getNestedMap(Map<String, Object> arguments, String name) {
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      Map<String, Object> map = castToMap(arguments.get(name));
      processSuppliers(map);
      return map;
    }
    return Map.of();
  }

  private static void processSuppliers(Map<String, Object> map) {
    map.entrySet()
        .forEach(entry -> {
          if (entry.getValue() instanceof Supplier) {
            entry.setValue(((Supplier) entry.getValue()).get());
          } else if (entry.getValue() instanceof Map) {
            Map<String, Object> childMap = castToMap(entry.getValue());
            processSuppliers(childMap);
          }
        });
  }
}
