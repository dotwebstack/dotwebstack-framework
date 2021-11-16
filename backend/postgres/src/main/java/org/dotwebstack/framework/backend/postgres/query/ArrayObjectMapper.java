package org.dotwebstack.framework.backend.postgres.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.backend.query.FieldMapper;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;

public class ArrayObjectMapper implements FieldMapper<Map<String, Object>, List<Object>> {

  protected final Map<String, ColumnMapper> fieldMappers = new HashMap<>();

  public ArrayObjectMapper() {}

  public void register(String name, ColumnMapper columnMapper) {
    fieldMappers.put(name, columnMapper);
  }

  @Override
  public List<Object> apply(Map<String, Object> row) {
    return fieldMappers.values()
        .stream()
        .findFirst()
        .map(ScalarFieldMapper.class::cast)
        .map(fieldMapper -> row.get(fieldMapper.getAlias()))
        .map(Object[].class::cast)
        .stream()
        .flatMap(firstArrayValue -> getCompositeObjects(row, firstArrayValue))
        .collect(Collectors.toList());
  }

  private Stream<Map<String, Object>> getCompositeObjects(Map<String, Object> row, Object[] firstArrayValue) {
    AtomicInteger counter = new AtomicInteger(0);
    return Arrays.stream(firstArrayValue)
        .map(v -> fieldMappers.keySet()
            .stream()
            .collect(Collectors.toMap(fieldName -> fieldName, fieldName -> {
              var fieldMapper = fieldMappers.get(fieldName);

              var data = Map.of(fieldMapper.getAlias(),
                  ((Object[]) row.get(fieldMapper.getAlias()))[counter.getAndIncrement()]);

              return fieldMappers.get(fieldName)
                  .apply(data);
            })));
  }
}
