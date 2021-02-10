package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.concurrent.atomic.AtomicInteger;

public class QueryContext {

  private final AtomicInteger aliasCounter = new AtomicInteger();

  public String newAlias() {
    return "x".concat(String.valueOf(aliasCounter.incrementAndGet()));
  }
}
