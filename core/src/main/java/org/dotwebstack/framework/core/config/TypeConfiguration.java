package org.dotwebstack.framework.core.config;

import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;

public interface TypeConfiguration<T extends AbstractFieldConfiguration> {

  List<KeyConfiguration> getKeys();

  Map<String, T> getFields();

  void init(Map<String, AbstractTypeConfiguration<?>> typeMapping, ObjectTypeDefinition objectTypeDefinition);

  KeyCondition getKeyCondition(DataFetchingEnvironment environment);

  KeyCondition getKeyCondition(String fieldName, Map<String, Object> source);

  KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source);

  String getUriTemplate();
}
