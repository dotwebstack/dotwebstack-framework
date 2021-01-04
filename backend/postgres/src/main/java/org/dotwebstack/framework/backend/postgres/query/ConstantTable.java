package org.dotwebstack.framework.backend.postgres.query;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public final class ConstantTable implements Table {

  @NonNull
  private final String columnName;

  @NonNull
  private final Set<Object> columnValues;

  @NonNull
  private final String alias;

  @Override
  public String toSql(VarBinder varBinder) {
    String values = columnValues.stream()
        .map(varBinder::register)
        .map(v -> String.format("(%s)", v))
        .collect(Collectors.joining(", "));

    return String.format("(VALUES %s) AS %s (%s)", values, getAlias(), getColumnName());
  }
}
