package org.dotwebstack.framework.backend.rdf4j.directives;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

public enum SparqlFilterOperator {

  EQ("="), NE("!="), GT(">"), GTE(">="), LT("<"), LTE("<=");

  @Getter
  private String value;

  SparqlFilterOperator(String value) {
    this.value = value;
  }

  public static Optional<SparqlFilterOperator> getByValue(String stringValue) {
    return Arrays.stream(values())
        .filter(value -> value.toString()
            .equals(stringValue))
        .findFirst();
  }

  public static SparqlFilterOperator getDefault() {
    return SparqlFilterOperator.EQ;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
