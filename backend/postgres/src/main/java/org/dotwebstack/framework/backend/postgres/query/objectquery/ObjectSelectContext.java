package org.dotwebstack.framework.backend.postgres.query.objectquery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Data;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;

@Data
public class ObjectSelectContext {

  private JoinTable joinTableConfiguration;

  private ObjectQueryContext objectQueryContext;

  private AtomicReference<String> checkNullAlias = new AtomicReference<>();

  private Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

  private Map<String, String> keyColumnNames;

  public ObjectSelectContext(ObjectQueryContext objectQueryContext) {
    this.objectQueryContext = objectQueryContext;
  }

  public ObjectSelectContext(ObjectQueryContext objectQueryContext, JoinTable joinTableConfiguration) {

    this.objectQueryContext = objectQueryContext;
    this.joinTableConfiguration = joinTableConfiguration;
  }

  public String newSelectAlias() {
    return objectQueryContext.newSelectAlias();
  }

  public String newTableAlias() {
    return objectQueryContext.newTableAlias();
  }

  public void setKeyColumnNames(Map<String, String> keyColumnNames) {
    this.keyColumnNames = keyColumnNames;
  }
}
