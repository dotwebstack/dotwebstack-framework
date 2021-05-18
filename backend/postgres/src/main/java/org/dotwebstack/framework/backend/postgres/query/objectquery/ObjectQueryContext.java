package org.dotwebstack.framework.backend.postgres.query.objectquery;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class ObjectQueryContext {

  private final AtomicInteger selectCounter = new AtomicInteger();

  private final AtomicInteger tableCounter = new AtomicInteger();

  public String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }

  public String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }
}
