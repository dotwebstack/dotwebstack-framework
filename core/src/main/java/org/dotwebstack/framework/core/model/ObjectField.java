package org.dotwebstack.framework.core.model;

import java.util.List;

public interface ObjectField {

  ObjectType<? extends ObjectField> getObjectType();

  void setObjectType(ObjectType<? extends ObjectField> objectType);

  String getName();

  void setName(String name);

  String getType();

  List<String> getKeys();

  boolean isList();

  boolean isNullable();

  boolean isPageable();

  List<FieldArgument> getArguments();

  String getAggregationOf();

  ObjectType<? extends ObjectField> getAggregationOfType();

  ObjectType<? extends ObjectField> getTargetType();
}
