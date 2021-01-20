package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapHelper {

  private MapHelper() {}

  public static Map<String, Object> getNestedMap(Map<String, Object> arguments, String name) {
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      return castToMap(arguments.get(name));
    }
    return ImmutableMap.of();
  }

  public static Map<String, Object> toGraphQlMap(Map<String, Object> dataMap, Map<String, Object> fieldAliasMap) {
    return fieldAliasMap.entrySet()
        .stream()
        .map(entry -> toGraphQlResultEntry(dataMap, entry))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @SuppressWarnings("unchecked")
  private static Optional<Map.Entry<String, Object>> toGraphQlResultEntry(Map<String, Object> dataMap,
      Map.Entry<String, Object> fieldAlias) {
    if (fieldAlias.getValue() instanceof Map) {
      return Optional
          .of(Map.entry(fieldAlias.getKey(), toGraphQlMap(dataMap, (Map<String, Object>) fieldAlias.getValue())));
    }

    if (fieldAlias.getValue() instanceof String) {
      String alias = (String) fieldAlias.getValue();

      if (dataMap.containsKey(alias) && dataMap.get(alias) != null) {
        return Optional.of(Map.entry(fieldAlias.getKey(), dataMap.get(alias)));
      }

      return Optional.empty();
    }

    throw illegalStateException("Only Map or String instance is allowed as entry value");
  }
}
