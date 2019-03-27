package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;

abstract class AbstractFetcherTest {

  DataFetchingEnvironment mockEnvironment(List<SelectedField> selectedFields,
      Map<String, Object> arguments) {
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(selectionSet.getFields()).thenReturn(selectedFields);

    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(environment.getArguments()).thenReturn(arguments);
    arguments.forEach((argKey, argValue) ->
        when(environment.getArgument(argKey)).thenReturn(argValue));

    return environment;
  }

  SelectedField mockSelectedField(String name) {
    SelectedField field = mock(SelectedField.class);
    when(field.getName()).thenReturn(name);

    return field;
  }

}
