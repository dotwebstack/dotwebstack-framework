package org.dotwebstack.framework.backend.postgres.query;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.Builder;
import lombok.Singular;

@Builder
public final class RowAssembler {

  @Singular
  private final Map<String, Function<Map<String, Object>, Object>> steps;

  public Map<String, Object> assemble(Map<String, Object> row) {
    return steps.entrySet()
        .stream()
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, stepEntry -> stepEntry.getValue()
            .apply(row)));
  }
}
