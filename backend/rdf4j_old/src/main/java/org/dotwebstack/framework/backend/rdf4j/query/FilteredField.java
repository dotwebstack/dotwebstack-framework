package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.SelectedField;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class FilteredField implements SelectedField {

  private FieldPath fieldPath;

  public FilteredField(FieldPath fieldPath) {
    this.fieldPath = fieldPath;
  }

  @Override
  public String getName() {
    return fieldPath.first()
        .getName();
  }

  @Override
  public String getQualifiedName() {
    return fieldPath.getFieldDefinitions()
        .stream()
        .map(GraphQLFieldDefinition::getName)
        .collect(Collectors.joining("/"));
  }

  @Override
  public GraphQLFieldDefinition getFieldDefinition() {
    return fieldPath.first();
  }

  @Override
  public Map<String, Object> getArguments() {
    return Collections.emptyMap();
  }

  @Override
  @SuppressWarnings("unchecked")
  public DataFetchingFieldSelectionSet getSelectionSet() {
    return fieldPath.rest()
        .map(restPath -> new DataFetchingFilterFieldSelectionSet(restPath))
        .orElse(new DataFetchingFilterFieldSelectionSet());
  }
}
