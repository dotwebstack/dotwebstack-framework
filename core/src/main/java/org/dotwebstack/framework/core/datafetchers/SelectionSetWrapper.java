package org.dotwebstack.framework.core.datafetchers;

import static org.dotwebstack.framework.core.datafetchers.FieldConstants.RDF_URI_FIELD;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.Delegate;

public class SelectionSetWrapper implements DataFetchingFieldSelectionSet {

  @Delegate(excludes = ExcludedSelectionSetMethods.class)
  private final DataFetchingFieldSelectionSet delegate;

  public SelectionSetWrapper(DataFetchingFieldSelectionSet delegate) {
    this.delegate = delegate;
  }

  public List<SelectedField> getFields(String fieldGlobPattern, String... fieldGlobPatterns) {
    return delegate.getFields(fieldGlobPattern, fieldGlobPatterns)
        .stream()
        .filter(field -> !RDF_URI_FIELD.equals(field.getName()))
        .collect(Collectors.toList());
  }

  private abstract class ExcludedSelectionSetMethods {

    public abstract List<SelectedField> getFields(String fieldGlobPattern, String... fieldGlobPatterns);

  }
}
