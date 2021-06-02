package org.dotwebstack.framework.backend.postgres.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class ObjectQueryContext {

  private Map<String, String> tableAliasByFieldName = new HashMap<>();

  private final AtomicInteger selectCounter = new AtomicInteger();

  private final AtomicInteger tableCounter = new AtomicInteger();

  public String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }

  public String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  public String newTableAlias(String fieldName) {
    var alias = "t".concat(String.valueOf(tableCounter.incrementAndGet()));
    tableAliasByFieldName.put(fieldName, alias);
    return alias;
  }

  public String getTableAlias(String fieldName){
    return tableAliasByFieldName.get(fieldName);
  }
}
