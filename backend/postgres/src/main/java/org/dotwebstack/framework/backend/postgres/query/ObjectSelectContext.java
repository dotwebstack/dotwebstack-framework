package org.dotwebstack.framework.backend.postgres.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Data;

@Data
public class ObjectSelectContext {

  private ObjectQueryContext objectQueryContext;

  private Map<String, String> fieldAliasMap = new HashMap<>();

  private AtomicReference<String> checkNullAlias = new AtomicReference<>();

  private Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

  private Map<String, String> keyColumnNames = new HashMap<>();

  public ObjectSelectContext() {
    this(new ObjectQueryContext());
  }

  public ObjectSelectContext(ObjectQueryContext objectQueryContext) {
    this.objectQueryContext = objectQueryContext;
  }

  public String newSelectAlias() {
    return objectQueryContext.newSelectAlias();
  }

  public String newTableAlias() {
    return objectQueryContext.newTableAlias();
  }

  public String newTableAlias(String fieldName) {
    return objectQueryContext.newTableAlias(fieldName);
  }

  public String getTableAlias(String fieldName) {
    return objectQueryContext.getTableAlias(fieldName);
  }

  public void setKeyColumnNames(Map<String, String> keyColumnNames) {
    this.keyColumnNames = keyColumnNames;
  }
}
