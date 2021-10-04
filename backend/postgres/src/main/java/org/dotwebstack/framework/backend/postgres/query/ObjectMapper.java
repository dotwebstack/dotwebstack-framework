package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.AbstractObjectMapper;

public class ObjectMapper extends AbstractObjectMapper<Map<String, Object>> {

  private final String alias;

  public ObjectMapper(String alias) {
    this.alias = alias;
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> row) {
    if (!row.containsKey(alias)) {
      return null;
    }

    return super.apply(row);
  }
}
