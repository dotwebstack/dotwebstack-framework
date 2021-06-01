package org.dotwebstack.framework.backend.postgres.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class ObjectQueryContext {

  private Map<String, String> tableAliasMap = new HashMap<>();

  private final AtomicInteger selectCounter = new AtomicInteger();

  private final AtomicInteger tableCounter = new AtomicInteger();

  public String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }

  public String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }
}
