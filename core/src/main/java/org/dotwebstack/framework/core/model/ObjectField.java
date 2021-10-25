package org.dotwebstack.framework.core.model;

import java.util.List;

public interface ObjectField {

  ObjectType<?> getObjectType();

  void setObjectType(ObjectType<?> objectType);

  String getName();

  void setName(String name);

  String getType();

  boolean isList();

  boolean isNullable();

  List<FieldArgument> getArguments();

  String getAggregationOf();

  ObjectType<?> getAggregationOfType();

  ObjectType<?> getTargetType();
}
