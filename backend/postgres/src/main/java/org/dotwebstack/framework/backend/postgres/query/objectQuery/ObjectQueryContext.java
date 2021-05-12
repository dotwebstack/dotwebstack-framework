package org.dotwebstack.framework.backend.postgres.query.objectQuery;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;

@Data
public class ObjectQueryContext {

  private final AtomicInteger selectCounter = new AtomicInteger();

  private AtomicReference<String> checkNullAlias = new AtomicReference<>();

  public String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}
