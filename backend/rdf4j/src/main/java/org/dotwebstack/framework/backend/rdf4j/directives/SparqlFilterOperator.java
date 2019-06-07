package org.dotwebstack.framework.backend.rdf4j.directives;

import lombok.Getter;

public enum SparqlFilterOperator {

  EQ("="), NE("!="), GT(">"), GTE(">="), LT("<"), LTE("<=");

  @Getter
  private String value;

  SparqlFilterOperator(String value) {
    this.value = value;
  }

  public static SparqlFilterOperator getDefault() {
    return SparqlFilterOperator.EQ;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
