package org.dotwebstack.framework.backend.postgres.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.backend.query.FieldMapper;

public class ArrayObjectMapper implements FieldMapper<Map<String, Object>, List<Object>> {

  protected final Map<String, ColumnMapper> fieldMappers = new HashMap<>();

  public ArrayObjectMapper() {}

  public void register(String name, ColumnMapper columnMapper) {
    fieldMappers.put(name, columnMapper);
  }

  @Override
  public List<Object> apply(Map<String, Object> row) {
    var first = fieldMappers.values()
        .stream()
        .findFirst()
        .map(fieldMapper -> row.get(fieldMapper.getAlias()))
        .map(Object[].class::cast)
        .stream()
        .flatMap(Arrays::stream);

    var index = new AtomicInteger(0);

    return first.map(firstArrayValue -> getCompositeObjects(row, index.getAndIncrement()))
        .collect(Collectors.toList());
  }

  private Map<String, Object> getCompositeObjects(Map<String, Object> row, int index) {
    return fieldMappers.keySet()
        .stream()
        .collect(Collectors.toMap(fieldName -> fieldName, fieldName -> {
          var fieldMapper = fieldMappers.get(fieldName);

          var data = Map.of(fieldMapper.getAlias(), ((Object[]) row.get(fieldMapper.getAlias()))[index]);

          return fieldMappers.get(fieldName)
              .apply(data);
        }));
  }
}
