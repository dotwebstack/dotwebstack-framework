package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public final class SchemaTable implements Table {

  @NonNull
  private final String name;

  @NonNull
  private final String alias;

  @Override
  public String toSql(VarBinder varBinder) {
    return String.format("%s AS %s", getName(), getAlias());
  }
}
