package org.dotwebstack.framework.core.datafetchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SelectionSetWrapperTest {

  @Test
  void getFields_returnFieldsWithoutUriIdentifier_forSelectionSetWithUriIdentifier() {
    DataFetchingFieldSelectionSet mockedDataFetchingFieldSelectionSet =
        mockDataFetchingFieldSelectionSet(Arrays.asList("_id", "name"));

    SelectionSetWrapper selectionSetWrapper = new SelectionSetWrapper(mockedDataFetchingFieldSelectionSet);

    List<SelectedField> fields = selectionSetWrapper.getFields("*.*");
    assertThat(fields.size(), is(1));
    assertFalse(containsFieldWithName(fields, "_id"));
  }

  @Test
  void getFields_returnFieldsWithoutUriIdentifier_for() {
    DataFetchingFieldSelectionSet mockedDataFetchingFieldSelectionSet =
        mockDataFetchingFieldSelectionSet(Arrays.asList("_id", "name"));

    SelectionSetWrapper selectionSetWrapper = new SelectionSetWrapper(mockedDataFetchingFieldSelectionSet);

    List<SelectedField> fields = selectionSetWrapper.getFields("*.*");
    assertThat(fields.size(), is(1));
    assertFalse(containsFieldWithName(fields, "_id"));
  }

  private boolean containsFieldWithName(List<SelectedField> fields, String name) {
    return fields.stream()
        .anyMatch(field -> field.getName()
            .equals(name));

  }

  private DataFetchingFieldSelectionSet mockDataFetchingFieldSelectionSet(List<String> selectedFieldNames) {
    DataFetchingFieldSelectionSet mockedDataFetchingFieldSelectionSet = mock(DataFetchingFieldSelectionSet.class);
    List<SelectedField> selectedFields = new ArrayList<>();
    selectedFieldNames.forEach(fieldName -> {
      SelectedField selectedField = mock(SelectedField.class);
      when(selectedField.getName()).thenReturn(fieldName);
      selectedFields.add(selectedField);
    });

    when(mockedDataFetchingFieldSelectionSet.getFields(anyString())).thenReturn(selectedFields);

    return mockedDataFetchingFieldSelectionSet;
  }
}
