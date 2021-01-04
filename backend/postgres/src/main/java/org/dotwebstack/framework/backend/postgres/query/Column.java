package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Column {

  private final Table table;

  private final String name;

  public String toSql() {
    return String.format("%s.%s", table.getAlias(), name);
  }
}
