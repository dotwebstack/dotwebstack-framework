package org.dotwebstack.framework.core.model;

import java.util.List;
import javax.validation.constraints.NotNull;

public interface ObjectField {

  ObjectType<?> getObjectType();

  void setObjectType(ObjectType<?> objectType);

  String getName();

  void setName(String name);

  String getType();

  @NotNull
  boolean isList();

  @NotNull
  boolean isNullable();

  @NotNull
  List<FieldArgument> getArguments();

  String getAggregationOf();

  ObjectType<?> getAggregationOfType();
}
