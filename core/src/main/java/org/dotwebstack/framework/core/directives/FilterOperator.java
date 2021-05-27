package org.dotwebstack.framework.core.directives;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

public enum FilterOperator {

  EQ("="), NE("!="), GT(">"), GTE(">="), LT("<"), LTE("<="), LANGUAGE("lang"), CONTAINS("contains"), ICONTAINS(
      "iContains");

  @Getter
  private final String value;

  FilterOperator(String value) {
    this.value = value;
  }

  public static Optional<FilterOperator> getByValue(String stringValue) {
    return Arrays.stream(values())
        .filter(value -> value.toString()
            .equals(stringValue))
        .findFirst();
  }

  public static FilterOperator getDefault() {
    return FilterOperator.EQ;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
