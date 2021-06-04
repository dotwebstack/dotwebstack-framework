package org.dotwebstack.framework.core.config;

import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.dotwebstack.framework.core.query.model.SortCriteria;

public interface TypeConfiguration<T extends AbstractFieldConfiguration> {

  List<KeyConfiguration> getKeys();

  Map<String, T> getFields();

  Optional<T> getField(String field);

  Map<String, FilterConfiguration> getFilters();

  Map<String, List<SortableByConfiguration>> getSortableBy();

  Map<String, List<SortCriteria>> getSortCriterias();

  void init(DotWebStackConfiguration dotWebStackConfiguration, ObjectTypeDefinition objectTypeDefinition);

  KeyCondition getKeyCondition(DataFetchingEnvironment environment);

  KeyCondition getKeyCondition(String fieldName, Map<String, Object> source);

  KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source);
}
