package org.dotwebstack.framework.core.backend.query;

import java.util.concurrent.atomic.AtomicInteger;

public class AliasManager {

  private final AtomicInteger aliasCounter = new AtomicInteger();

  public String newAlias() {
    return "x".concat(String.valueOf(aliasCounter.incrementAndGet()));
  }
}
