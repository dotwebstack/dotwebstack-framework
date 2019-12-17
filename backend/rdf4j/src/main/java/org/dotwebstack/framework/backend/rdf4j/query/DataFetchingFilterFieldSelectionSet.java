package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.execution.MergedSelectionSet;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.SelectedField;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataFetchingFilterFieldSelectionSet implements DataFetchingFieldSelectionSet {

  private FieldPath fieldPath;

  public DataFetchingFilterFieldSelectionSet(FieldPath fieldPath) {
    this.fieldPath = fieldPath;
  }

  public DataFetchingFilterFieldSelectionSet() {

  }

  @Override
  public MergedSelectionSet get() {
    return null;
  }

  @Override
  public Map<String, Map<String, Object>> getArguments() {
    return null;
  }

  @Override
  public Map<String, GraphQLFieldDefinition> getDefinitions() {
    return null;
  }

  @Override
  public boolean contains(String s) {
    return false;
  }

  @Override
  public boolean containsAnyOf(String s, String... strings) {
    return false;
  }

  @Override
  public boolean containsAllOf(String s, String... strings) {
    return false;
  }

  @Override
  public List<SelectedField> getFields() {
    if (Objects.nonNull(fieldPath)) {
      return Collections.singletonList(new FilteredField(fieldPath));
    }
    return Collections.emptyList();
  }

  @Override
  public List<SelectedField> getFields(String s) {
    return null;
  }

  @Override
  public SelectedField getField(String s) {
    return null;
  }
}
