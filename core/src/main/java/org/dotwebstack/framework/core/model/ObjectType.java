package org.dotwebstack.framework.core.model;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

public interface ObjectType<T extends ObjectField> {

  String getName();

  void setName(String name);

  Map<String, T> getFields();

  T getField(String name);

  Map<String, List<SortableByConfiguration>> getSortableBy();

  Map<String, FilterConfiguration> getFilters();

  boolean isNested();
}
