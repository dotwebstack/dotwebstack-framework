package org.dotwebstack.framework.backend.postgres.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.backend.query.FieldMapper;

public class IdentifyingObjectMapper implements FieldMapper<Map<String, Object>, Object> {

  private final Map<String, String> mapping;

  public IdentifyingObjectMapper(Map<String, String> mapping) {
    this.mapping = mapping;
  }

  @Override
  public Object apply(Map<String, Object> row) {
    List<Object> list = new ArrayList<>();

    var name = StringUtils.substringBefore(mapping.keySet()
        .iterator()
        .next(), ".");
    var value = row.get(mapping.values()
        .iterator()
        .next());

    if (value != null) {
      var size = ((Object[]) value).length;

      for (int i = 0; i < size; i++) {
        final int index = i;

        var val = mapping.entrySet()
            .stream()
            .collect(Collectors.toMap(mappingItem -> StringUtils.substringAfter(mappingItem.getKey(), "."),
                mappingItem -> ((Object[]) row.get(mappingItem.getValue()))[index]));

        list.add(val);
      }
    }

    return Map.of(name, list);
  }
}
