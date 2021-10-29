package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.AbstractObjectMapper;

public class ObjectMapper extends AbstractObjectMapper<Map<String, Object>> {

  @Getter
  private String alias;

  public ObjectMapper() {}

  public ObjectMapper(String alias) {
    this.alias = alias;
  }

  @Override
  @SuppressWarnings("squid:S1168")
  public Map<String, Object> apply(Map<String, Object> row) {
    if (alias != null && row.get(alias) == null) {
      return null;
    }

    return super.apply(row);
  }
}
