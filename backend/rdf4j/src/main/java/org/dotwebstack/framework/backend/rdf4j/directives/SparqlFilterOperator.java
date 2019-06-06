package org.dotwebstack.framework.backend.rdf4j.directives;

import java.util.Arrays;

public enum SparqlFilterOperator {

  EQ("="), NE("!="), GT(">"), GTE(">="), LT("<"), LTE("<=");

  private String value;

  SparqlFilterOperator(String value) {
    this.value = value;
  }

  public static SparqlFilterOperator getByValue(String stringValue) {
    return Arrays.stream(values())
        .filter(value -> value.toString()
            .equals(stringValue))
        .findFirst()
        .orElse(null);
  }

  public static SparqlFilterOperator getDefault() {
    return SparqlFilterOperator.EQ;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
