package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.AbstractObjectMapper;

public class ObjectMapper extends AbstractObjectMapper<Map<String, Object>> {

  @Getter
  private String alias;

  @Getter
  private String presenceAlias;

  public ObjectMapper() {}

  public ObjectMapper(String alias) {
    this.alias = alias;
  }

  public ObjectMapper(String alias, String presenceAlias) {
    this.alias = alias;
    this.presenceAlias = presenceAlias;
  }

  @Override
  @SuppressWarnings("squid:S1168")
  public Map<String, Object> apply(Map<String, Object> row) {
    if (isAliasResultNull(row) || isPresenceAliasResultFalse(row)) {
      return null;
    }

    return super.apply(row);
  }

  private boolean isAliasResultNull(Map<String, Object> row) {
    return alias != null && row.get(alias) == null;
  }

  private boolean isPresenceAliasResultFalse(Map<String, Object> row) {
    return presenceAlias != null
        && (row.get(presenceAlias) == null || Boolean.FALSE.equals(Boolean.valueOf(row.get(presenceAlias)
            .toString())));
  }
}
