package org.dotwebstack.framework.core.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

public interface ObjectType<T extends ObjectField> {

  String getName();

  void setName(String name);

  Map<String, T> getFields();

  Optional<T> getField(String name);

  Map<String, List<SortableByConfiguration>> getSortableBy();
}
