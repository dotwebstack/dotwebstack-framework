package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

public class QueryContext {

  @Getter
  private final DataFetchingFieldSelectionSet selectionSet;

  private final AtomicInteger tableCounter = new AtomicInteger();

  private final AtomicInteger selectCounter = new AtomicInteger();

  public QueryContext(DataFetchingFieldSelectionSet selectionSet) {
    this.selectionSet = selectionSet;
  }

  public String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  public String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}
