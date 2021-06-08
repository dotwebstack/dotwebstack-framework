package org.dotwebstack.framework.backend.postgres.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Data;

@Data
public class ObjectSelectContext {

  private ObjectQueryContext objectQueryContext;

  private AtomicReference<String> checkNullAlias = new AtomicReference<>();

  private boolean useNullMapWhenNotFound = false;

  private List<PostgresKeyCriteria> joinCriteria = new ArrayList<>();

  private Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

  private Map<String, String> keyColumnNames = new HashMap<>();

  public ObjectSelectContext() {
    this(new ObjectQueryContext());
  }

  public ObjectSelectContext(ObjectQueryContext objectQueryContext) {
    this.objectQueryContext = objectQueryContext;
  }

  public ObjectSelectContext(List<PostgresKeyCriteria> joinCriteria, boolean useNullMapWhenNotFound) {
    this(new ObjectQueryContext());
    this.joinCriteria = joinCriteria;
    this.useNullMapWhenNotFound = useNullMapWhenNotFound;
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
