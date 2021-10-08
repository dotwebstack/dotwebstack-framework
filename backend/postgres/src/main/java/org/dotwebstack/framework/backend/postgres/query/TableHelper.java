package org.dotwebstack.framework.backend.postgres.query;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class TableHelper {

  private TableHelper() {}

  private static Table<Record> createTable(String name, ContextCriteria contextCriteria) {
    AtomicInteger atomicInteger = new AtomicInteger(0);

    String bindingKeys = contextCriteria.getValues()
        .keySet()
        .stream()
        .map(key -> String.format("{%d}", atomicInteger.getAndIncrement()))
        .collect(Collectors.joining(","));

    Object[] bindingValues = new ArrayList<>(contextCriteria.getValues()
        .values()).toArray(Object[]::new);

    return DSL.table(String.format("%s_%s_ctx(%s)", name, contextCriteria.getName(), bindingKeys), bindingValues);
  }

  public static Table<Record> findTable(String name, ContextCriteria contextCriteria) {
    if (contextCriteria != null) {
      return createTable(name, contextCriteria);
    }
    return DSL.table(DSL.name(name.split("\\.")));
  }
}
