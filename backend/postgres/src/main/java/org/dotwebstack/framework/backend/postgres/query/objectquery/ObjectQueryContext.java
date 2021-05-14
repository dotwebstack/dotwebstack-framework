package org.dotwebstack.framework.backend.postgres.query.objectquery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Data;

@Data
public class ObjectQueryContext {

  private final AtomicInteger selectCounter = new AtomicInteger();

  private final AtomicInteger tableCounter = new AtomicInteger();

  private AtomicReference<String> checkNullAlias = new AtomicReference<>();

  public String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }

  public String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }
}
