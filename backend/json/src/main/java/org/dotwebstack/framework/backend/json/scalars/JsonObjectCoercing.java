package org.dotwebstack.framework.backend.json.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import java.util.Map;

public class JsonObjectCoercing implements Coercing<Map<?, ?>, Map<?, ?>> {
  @Override
  public Map<?, ?> serialize(Object map) {
    if (map instanceof Map) {
      return (Map<?, ?>) map;
    }
    throw new IllegalArgumentException("Only supports map implementations!");
  }

  @Override
  public Map<?, ?> parseValue(Object map) {
    return parseMap(map);
  }

  @Override
  public Map<?, ?> parseLiteral(Object map) {
    return parseMap(map);
  }

  private Map<?, ?> parseMap(Object map) {
    if (map instanceof Map) {
      return (Map<?, ?>) map;
    }
    throw new CoercingParseValueException(String.format("Unable to parse Map from '%s' type.", map.getClass()
        .getName()));
  }
}
