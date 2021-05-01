package org.dotwebstack.framework.core.config;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface FieldConfiguration {

  @NotBlank
  String getType();

  @NotNull
  boolean isNullable();

  @NotNull
  boolean isList();

  String getMappedBy();

  String getAggregationOf();
}
