package org.dotwebstack.framework.core.config;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface FieldConfiguration {

  @NotBlank
  String getType();

  @NotNull
  boolean isNullable();

  @NotNull
  boolean isList();

  @NotNull
  List<FieldArgumentConfiguration> getArguments();

  String getMappedBy();

  String getAggregationOf();
}
