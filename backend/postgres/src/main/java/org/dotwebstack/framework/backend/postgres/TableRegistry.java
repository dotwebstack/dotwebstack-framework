package org.dotwebstack.framework.backend.postgres;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public class TableRegistry {

  private final Map<String, TableMapping> tableMappings = new HashMap<>();

  public void register(String typeName, TableMapping tableMapping) {
    tableMappings.put(typeName, tableMapping);
  }

  public TableMapping get(String typeName) {
    return tableMappings.get(typeName);
  }

  public boolean contains(String typeName) {
    return tableMappings.containsKey(typeName);
  }

  @Builder
  @Getter
  public static class TableMapping {

    @NonNull
    private final Table<Record> table;

    @NonNull
    private final Field<Object> keyColumn;
  }
}
