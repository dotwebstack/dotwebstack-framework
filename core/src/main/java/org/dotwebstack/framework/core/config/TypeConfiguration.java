package org.dotwebstack.framework.core.config;

import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Map;

public interface TypeConfiguration<T extends AbstractFieldConfiguration> {

  List<KeyConfiguration> getKeys();

  Map<String, T> getFields();

  void init(TypeDefinitionRegistry typeDefinitionRegistry);
}
