package org.dotwebstack.framework.core.model;

import java.util.List;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;

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

  ObjectType<? extends ObjectField> getTargetType();

  FieldEnumConfiguration getEnumeration();

  String getValueFetcher();

  boolean isEnumeration();
}
