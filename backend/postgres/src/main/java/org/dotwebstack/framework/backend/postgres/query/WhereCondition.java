package org.dotwebstack.framework.backend.postgres.query;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public final class WhereCondition {

  @NonNull
  private final Column column;

  @NonNull
  private final Object value;

  @NonNull
  @Builder.Default
  private final Operator operator = Operator.EQ;

  public String toSQL(VarBinder varBinder) {
    return String.format("%s %s %s",
        column.toSql(), operator.getSymbol(), valueExpr(varBinder));
  }

  private String valueExpr(VarBinder varBinder) {
    if (value instanceof Collection) {
      return String.format("(%s)", ((Collection<?>) value).stream()
          .map(varBinder::register)
          .collect(Collectors.joining(", ")));
    }

    return varBinder.register(value);
  }

  @Getter
  public enum Operator {
    EQ("="),
    IN("IN");

    private final String symbol;

    Operator(String symbol) {
      this.symbol = symbol;
    }
  }
}
