package org.dotwebstack.framework.backend.postgres.query;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class TableHelper {

  private TableHelper() {}

  public static Table<Record> createTable(String name, List<ContextCriteria> contextCriterias) {
    AtomicInteger atomicInteger = new AtomicInteger(0);

    String bindingKeys = contextCriterias.stream()
        .map(contextCriteria -> String.format("{%d}", atomicInteger.getAndIncrement()))
        .collect(Collectors.joining(","));

    Object[] bindingValues = contextCriterias.stream()
        .map(ContextCriteria::getValue)
        .collect(Collectors.toList())
        .toArray(Object[]::new);

    return DSL.table(String.format("%s_ctx(%s)", name, bindingKeys), bindingValues);
  }

  public static Table<Record> findTable(String name, List<ContextCriteria> contextCriteria) {
    if (!contextCriteria.isEmpty()) {
      return createTable(name, contextCriteria);
    }
    return DSL.table(DSL.name(name.split("\\.")));
  }
}
