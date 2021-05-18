package org.dotwebstack.framework.backend.postgres.query.objectquery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Data;

@Data
public class ObjectSelectContext {

  private ObjectQueryContext objectQueryContext;

  private AtomicReference<String> checkNullAlias = new AtomicReference<>();

  private Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

  public ObjectSelectContext(ObjectQueryContext objectQueryContext) {
    this.objectQueryContext = objectQueryContext;
  }

  public String newSelectAlias() {
    return objectQueryContext.newSelectAlias();
  }

  public String newTableAlias() {
    return objectQueryContext.newTableAlias();
  }
}
