package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ContextArgumentType {

  DATE("Date"), DATETIME("DateTime"), STRING("String"), BOOLEAN("Boolean"), INT("Int"), FLOAT("Float");

  private final String val;

  ContextArgumentType(String val) {
    this.val = val;
  }

  @Override
  public String toString() {
    return value();
  }

  @JsonValue
  public final String value() {
    return this.val;
  }
}
