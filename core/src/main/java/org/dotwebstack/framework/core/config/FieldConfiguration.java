package org.dotwebstack.framework.core.config;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

// TODO: fix adding name, implemet adding name in CoreConfigurer or AbstractFieldConfiguration via setter method ('setFields')
public interface FieldConfiguration {

  String getName();

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
